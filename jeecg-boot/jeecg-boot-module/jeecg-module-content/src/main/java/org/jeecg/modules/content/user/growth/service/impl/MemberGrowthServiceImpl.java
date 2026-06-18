package org.jeecg.modules.content.user.growth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.user.growth.constant.GrowthConstant;
import org.jeecg.modules.content.user.growth.entity.CircleAchievement;
import org.jeecg.modules.content.user.growth.entity.CircleGrowthLog;
import org.jeecg.modules.content.user.growth.entity.CircleMemberAchievement;
import org.jeecg.modules.content.user.growth.entity.CircleMemberGrowth;
import org.jeecg.modules.content.user.growth.enums.GrowthActionEnum;
import org.jeecg.modules.content.user.growth.mapper.CircleAchievementMapper;
import org.jeecg.modules.content.user.growth.mapper.CircleGrowthLogMapper;
import org.jeecg.modules.content.user.growth.mapper.CircleMemberAchievementMapper;
import org.jeecg.modules.content.user.growth.mapper.CircleMemberGrowthMapper;
import org.jeecg.modules.content.user.growth.service.IMemberGrowthService;
import org.jeecg.modules.content.user.growth.vo.AchievementVO;
import org.jeecg.modules.content.user.growth.vo.MemberGrowthVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MemberGrowthServiceImpl extends ServiceImpl<CircleMemberGrowthMapper, CircleMemberGrowth>
        implements IMemberGrowthService {

    @Resource
    private CircleGrowthLogMapper growthLogMapper;
    @Resource
    private CircleMemberAchievementMapper memberAchievementMapper;
    @Resource
    private CircleAchievementMapper achievementMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addExperience(String circleId, String userId, GrowthActionEnum action, String bizId) {
        LocalDate today = LocalDate.now();

        if (isDailyCapReached(circleId, userId, today)) {
            log.info("用户{}在圈子{}今日经验值已达上限", userId, circleId);
            return;
        }

        CircleGrowthLog growthLog = new CircleGrowthLog()
                .setCircleId(circleId)
                .setUserId(userId)
                .setActionType(action.getCode())
                .setExpPoints(action.getExpPoints())
                .setContributionPoints(action.getContributionPoints())
                .setBizId(bizId)
                .setBizDate(today)
                .setRevoked(false);
        growthLogMapper.insert(growthLog);

        CircleMemberGrowth growth = getOrCreateGrowth(circleId, userId);
        growth.setExpPoints(growth.getExpPoints() + action.getExpPoints());
        growth.setContributionPoints(growth.getContributionPoints() + action.getContributionPoints());
        if (action == GrowthActionEnum.POST) {
            growth.setPostCount(growth.getPostCount() + 1);
        } else if (action == GrowthActionEnum.COMMENT) {
            growth.setCommentCount(growth.getCommentCount() + 1);
        } else if (action == GrowthActionEnum.FEATURED) {
            growth.setFeaturedCount(growth.getFeaturedCount() + 1);
        }
        this.saveOrUpdate(growth);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeExperience(String circleId, String userId, GrowthActionEnum action, String bizId) {
        LambdaQueryWrapper<CircleGrowthLog> logQw = new LambdaQueryWrapper<>();
        logQw.eq(CircleGrowthLog::getCircleId, circleId)
              .eq(CircleGrowthLog::getUserId, userId)
              .eq(CircleGrowthLog::getActionType, action.getCode())
              .eq(CircleGrowthLog::getBizId, bizId)
              .eq(CircleGrowthLog::getRevoked, false);
        CircleGrowthLog existingLog = growthLogMapper.selectOne(logQw);
        if (existingLog == null) {
            log.warn("未找到可撤销的流水记录: circleId={}, userId={}, action={}, bizId={}", circleId, userId, action, bizId);
            return;
        }

        existingLog.setRevoked(true);
        growthLogMapper.updateById(existingLog);

        CircleMemberGrowth growth = getOrCreateGrowth(circleId, userId);
        growth.setExpPoints(Math.max(0, growth.getExpPoints() - action.getExpPoints()));
        growth.setContributionPoints(Math.max(0, growth.getContributionPoints() - action.getContributionPoints()));
        if (action == GrowthActionEnum.POST) {
            growth.setPostCount(Math.max(0, growth.getPostCount() - 1));
        } else if (action == GrowthActionEnum.COMMENT) {
            growth.setCommentCount(Math.max(0, growth.getCommentCount() - 1));
        } else if (action == GrowthActionEnum.FEATURED) {
            growth.setFeaturedCount(Math.max(0, growth.getFeaturedCount() - 1));
        }
        this.updateById(growth);
    }

    @Override
    public MemberGrowthVO getGrowthInfo(String circleId, String userId) {
        CircleMemberGrowth growth = getOrCreateGrowth(circleId, userId);
        MemberGrowthVO vo = new MemberGrowthVO();
        vo.setCircleId(circleId);
        vo.setExpPoints(growth.getExpPoints());
        vo.setContributionPoints(growth.getContributionPoints());
        vo.setLevel(growth.getLevel());
        vo.setPostCount(growth.getPostCount());
        vo.setParticipationDays(getParticipationDays(circleId, userId));

        // 计算圈子内排名：经验值高于当前用户的成员数 + 1
        LambdaQueryWrapper<CircleMemberGrowth> rankQw = new LambdaQueryWrapper<>();
        rankQw.eq(CircleMemberGrowth::getCircleId, circleId)
              .gt(CircleMemberGrowth::getExpPoints, growth.getExpPoints());
        long higherCount = this.baseMapper.selectCount(rankQw);
        vo.setRank((int) higherCount + 1);

        // 计算下一等级进度
        int currentLevel = growth.getLevel() != null ? growth.getLevel() : 1;
        int currentThreshold = GrowthConstant.LEVEL_THRESHOLDS[Math.min(currentLevel - 1, GrowthConstant.LEVEL_THRESHOLDS.length - 1)];
        int nextThreshold = currentLevel < GrowthConstant.LEVEL_THRESHOLDS.length
                ? GrowthConstant.LEVEL_THRESHOLDS[currentLevel]
                : currentThreshold;
        vo.setNextLevelThreshold(nextThreshold);
        if (nextThreshold > currentThreshold) {
            int exp = growth.getExpPoints() != null ? growth.getExpPoints() : 0;
            vo.setProgressPercent(Math.min((exp - currentThreshold) * 100 / (nextThreshold - currentThreshold), 100));
        } else {
            vo.setProgressPercent(100);
        }

        // 今日已获经验值
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<CircleGrowthLog> todayQw = new LambdaQueryWrapper<>();
        todayQw.eq(CircleGrowthLog::getCircleId, circleId)
               .eq(CircleGrowthLog::getUserId, userId)
               .eq(CircleGrowthLog::getBizDate, today)
               .eq(CircleGrowthLog::getRevoked, false);
        int todayExp = growthLogMapper.selectList(todayQw).stream()
                .mapToInt(log -> log.getExpPoints() != null ? log.getExpPoints() : 0)
                .sum();
        vo.setTodayExp(todayExp);
        vo.setDailyExpLimit(GrowthConstant.DAILY_EXP_CAP);

        // 最近获得的徽章（最多3枚）
        LambdaQueryWrapper<CircleMemberAchievement> badgeQw = new LambdaQueryWrapper<>();
        badgeQw.eq(CircleMemberAchievement::getCircleId, circleId)
               .eq(CircleMemberAchievement::getUserId, userId)
               .eq(CircleMemberAchievement::getRevoked, false)
               .orderByDesc(CircleMemberAchievement::getCreateTime)
               .last("LIMIT 3");
        List<CircleMemberAchievement> recentEarned = memberAchievementMapper.selectList(badgeQw);
        List<AchievementVO> recentBadges = new ArrayList<>();
        for (CircleMemberAchievement ma : recentEarned) {
            CircleAchievement def = achievementMapper.selectOne(
                    new LambdaQueryWrapper<CircleAchievement>()
                            .eq(CircleAchievement::getAchievementType, ma.getAchievementType()));
            if (def != null) {
                AchievementVO badgeVo = new AchievementVO();
                badgeVo.setAchievementType(def.getAchievementType());
                badgeVo.setName(def.getName());
                badgeVo.setDescription(def.getDescription());
                badgeVo.setIconUrl(def.getIconUrl());
                badgeVo.setEarned(true);
                recentBadges.add(badgeVo);
            }
        }
        vo.setRecentBadges(recentBadges);

        return vo;
    }

    @Override
    public int getParticipationDays(String circleId, String userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);
        LambdaQueryWrapper<CircleGrowthLog> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleGrowthLog::getCircleId, circleId)
          .eq(CircleGrowthLog::getUserId, userId)
          .eq(CircleGrowthLog::getRevoked, false)
          .between(CircleGrowthLog::getBizDate, weekAgo, today)
          .select(CircleGrowthLog::getBizDate)
          .groupBy(CircleGrowthLog::getBizDate);
        List<CircleGrowthLog> logs = growthLogMapper.selectList(qw);
        return logs.size();
    }

    private boolean isDailyCapReached(String circleId, String userId, LocalDate date) {
        LambdaQueryWrapper<CircleGrowthLog> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleGrowthLog::getCircleId, circleId)
          .eq(CircleGrowthLog::getUserId, userId)
          .eq(CircleGrowthLog::getBizDate, date)
          .eq(CircleGrowthLog::getRevoked, false);
        List<CircleGrowthLog> todayLogs = growthLogMapper.selectList(qw);
        int dailyTotal = todayLogs.stream().mapToInt(CircleGrowthLog::getExpPoints).sum();
        return dailyTotal >= GrowthConstant.DAILY_EXP_CAP;
    }

    private CircleMemberGrowth getOrCreateGrowth(String circleId, String userId) {
        LambdaQueryWrapper<CircleMemberGrowth> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleMemberGrowth::getCircleId, circleId)
          .eq(CircleMemberGrowth::getUserId, userId);
        CircleMemberGrowth growth = this.getOne(qw);
        if (growth == null) {
            growth = new CircleMemberGrowth()
                    .setCircleId(circleId)
                    .setUserId(userId)
                    .setExpPoints(0)
                    .setContributionPoints(0)
                    .setLevel(1)
                    .setPostCount(0)
                    .setCommentCount(0)
                    .setFeaturedCount(0);
            try {
                this.save(growth);
            } catch (Exception e) {
                // 并发场景下另一个线程已插入，重新查询
                growth = this.getOne(qw);
            }
        }
        return growth;
    }
}
