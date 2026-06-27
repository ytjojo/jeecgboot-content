package org.jeecg.modules.content.circle.growth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.content.circle.growth.entity.CircleAchievement;
import org.jeecg.modules.content.circle.growth.entity.CircleMemberAchievement;
import org.jeecg.modules.content.circle.growth.entity.CircleMemberGrowth;
import org.jeecg.modules.content.circle.growth.enums.AchievementTypeEnum;
import org.jeecg.modules.content.circle.growth.mapper.CircleAchievementMapper;
import org.jeecg.modules.content.circle.growth.mapper.CircleInviteRecordMapper;
import org.jeecg.modules.content.circle.growth.mapper.CircleMemberAchievementMapper;
import org.jeecg.modules.content.circle.growth.mapper.CircleMemberGrowthMapper;
import org.jeecg.modules.content.circle.growth.service.IAchievementService;
import org.jeecg.modules.content.circle.growth.service.IMemberGrowthService;
import org.jeecg.modules.content.circle.growth.vo.AchievementVO;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AchievementServiceImpl extends ServiceImpl<CircleMemberAchievementMapper, CircleMemberAchievement>
        implements IAchievementService {

    @Resource
    private CircleMemberGrowthMapper growthMapper;
    @Resource
    private CircleAchievementMapper achievementMapper;
    @Resource
    private CircleInviteRecordMapper inviteRecordMapper;
    @Resource
    private IMemberGrowthService memberGrowthService;
    @Resource
    private IContentNotificationService notificationService;
    @Resource
    private ISysBaseAPI sysBaseAPI;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void checkAndAward(String circleId, String userId) {
        CircleMemberGrowth growth = getGrowth(circleId, userId);
        if (growth == null) return;

        if (growth.getPostCount() != null && growth.getPostCount() >= 10) {
            tryAward(circleId, userId, AchievementTypeEnum.CONTINUOUS_CREATOR);
        }
        if (growth.getFeaturedCount() != null && growth.getFeaturedCount() >= 5) {
            tryAward(circleId, userId, AchievementTypeEnum.QUALITY_CONTRIBUTOR);
        }
        int participationDays = memberGrowthService.getParticipationDays(circleId, userId);
        if (participationDays >= 3) {
            tryAward(circleId, userId, AchievementTypeEnum.ACTIVE_PARTICIPANT);
        }
        if (growth.getPostCount() != null && growth.getPostCount() >= 1) {
            LambdaQueryWrapper<CircleMemberGrowth> rankQw = new LambdaQueryWrapper<>();
            rankQw.eq(CircleMemberGrowth::getCircleId, circleId)
                  .gt(CircleMemberGrowth::getExpPoints, growth.getExpPoints());
            long higherCount = growthMapper.selectCount(rankQw);
            if (higherCount < 10) {
                tryAward(circleId, userId, AchievementTypeEnum.RISING_STAR);
            } else {
                revoke(circleId, userId, AchievementTypeEnum.RISING_STAR);
            }
        }

        // 内容里程碑：累计发布 50 篇可见内容
        if (growth.getPostCount() != null && growth.getPostCount() >= 50) {
            tryAward(circleId, userId, AchievementTypeEnum.CONTENT_MILESTONE);
        }

        // 社交达人：邀请 5 人加入圈子
        if (inviteRecordMapper != null) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.content.circle.growth.entity.CircleInviteRecord> inviteQw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            inviteQw.eq(org.jeecg.modules.content.circle.growth.entity.CircleInviteRecord::getCircleId, circleId)
                    .eq(org.jeecg.modules.content.circle.growth.entity.CircleInviteRecord::getInviterId, userId)
                    .eq(org.jeecg.modules.content.circle.growth.entity.CircleInviteRecord::getStatus, "JOINED");
            long inviteCount = inviteRecordMapper.selectCount(inviteQw);
            if (inviteCount >= 5) {
                tryAward(circleId, userId, AchievementTypeEnum.SOCIAL_BUTTERFLY);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(String circleId, String userId, AchievementTypeEnum type) {
        LambdaQueryWrapper<CircleMemberAchievement> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleMemberAchievement::getCircleId, circleId)
          .eq(CircleMemberAchievement::getUserId, userId)
          .eq(CircleMemberAchievement::getAchievementType, type.getCode())
          .eq(CircleMemberAchievement::getRevoked, false);
        CircleMemberAchievement achievement = this.getOne(qw);
        if (achievement == null) return;

        achievement.setRevoked(true);
        this.updateById(achievement);
    }

    @Override
    public List<AchievementVO> getMemberAchievements(String circleId, String userId) {
        List<CircleAchievement> allAchievements = achievementMapper.selectList(null);
        LambdaQueryWrapper<CircleMemberAchievement> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleMemberAchievement::getCircleId, circleId)
          .eq(CircleMemberAchievement::getUserId, userId)
          .eq(CircleMemberAchievement::getRevoked, false);
        List<CircleMemberAchievement> earned = this.list(qw);
        Set<String> earnedTypes = earned.stream()
                .map(CircleMemberAchievement::getAchievementType)
                .collect(Collectors.toSet());
        Map<String, java.util.Date> earnedDateMap = earned.stream()
                .collect(Collectors.toMap(
                        CircleMemberAchievement::getAchievementType,
                        CircleMemberAchievement::getCreateTime,
                        (d1, d2) -> d1));

        // 加载用户成长数据用于计算进度
        CircleMemberGrowth growth = getGrowth(circleId, userId);
        int participationDays = memberGrowthService.getParticipationDays(circleId, userId);
        // RISING_STAR 需要排名：查询经验值高于当前用户的成员数
        long higherCount = 0;
        if (growth != null && growth.getExpPoints() != null) {
            LambdaQueryWrapper<CircleMemberGrowth> rankQw = new LambdaQueryWrapper<>();
            rankQw.eq(CircleMemberGrowth::getCircleId, circleId)
                  .gt(CircleMemberGrowth::getExpPoints, growth.getExpPoints());
            higherCount = growthMapper.selectCount(rankQw);
        }

        List<AchievementVO> result = new ArrayList<>();
        for (CircleAchievement a : allAchievements) {
            AchievementVO vo = new AchievementVO();
            vo.setAchievementType(a.getAchievementType());
            vo.setName(a.getName());
            vo.setDescription(a.getDescription());
            vo.setIconUrl(a.getIconUrl());
            vo.setConditionDesc(a.getConditionDesc());
            vo.setEarned(earnedTypes.contains(a.getAchievementType()));
            vo.setEarnedDate(earnedDateMap.get(a.getAchievementType()));

            // 计算进度与状态
            if (growth != null) {
                int progress = 0;
                int target = 0;
                switch (a.getAchievementType()) {
                    case "CONTINUOUS_CREATOR":
                        target = 10;
                        progress = Math.min(growth.getPostCount() != null ? growth.getPostCount() : 0, 10);
                        break;
                    case "QUALITY_CONTRIBUTOR":
                        target = 5;
                        progress = Math.min(growth.getFeaturedCount() != null ? growth.getFeaturedCount() : 0, 5);
                        break;
                    case "ACTIVE_PARTICIPANT":
                        target = 3;
                        progress = Math.min(participationDays, 3);
                        break;
                    case "RISING_STAR":
                        target = 10;
                        int rank = (int) higherCount + 1;
                        // rank 越低越好，直接用原始排名，status 计算时用 target/current 判定
                        progress = rank;
                        break;
                    case "CONTENT_MILESTONE":
                        target = 50;
                        progress = Math.min(growth.getPostCount() != null ? growth.getPostCount() : 0, 50);
                        break;
                    case "SOCIAL_BUTTERFLY":
                        target = 5;
                        if (inviteRecordMapper != null) {
                            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.content.circle.growth.entity.CircleInviteRecord> inviteQw2 = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                            inviteQw2.eq(org.jeecg.modules.content.circle.growth.entity.CircleInviteRecord::getCircleId, circleId)
                                    .eq(org.jeecg.modules.content.circle.growth.entity.CircleInviteRecord::getInviterId, userId)
                                    .eq(org.jeecg.modules.content.circle.growth.entity.CircleInviteRecord::getStatus, "JOINED");
                            long inviteCount2 = inviteRecordMapper.selectCount(inviteQw2);
                            progress = Math.min((int) inviteCount2, 5);
                        }
                        break;
                    default:
                        break;
                }
                vo.setCurrentProgress(progress);
                vo.setTargetProgress(target);

                // 状态计算：RISING_STAR 排名越低越好，用 target/current 判定
                if (Boolean.TRUE.equals(vo.getEarned())) {
                    vo.setStatus("EARNED");
                } else if (target > 0 && progress > 0) {
                    double ratio = "RISING_STAR".equals(a.getAchievementType())
                            ? (double) target / progress
                            : (double) progress / target;
                    vo.setStatus(ratio >= 0.8 ? "CLOSE" : "UNEARNED");
                } else {
                    vo.setStatus("UNEARNED");
                }
            }

            result.add(vo);
        }
        return result;
    }

    private void tryAward(String circleId, String userId, AchievementTypeEnum type) {
        LambdaQueryWrapper<CircleMemberAchievement> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleMemberAchievement::getCircleId, circleId)
          .eq(CircleMemberAchievement::getUserId, userId)
          .eq(CircleMemberAchievement::getAchievementType, type.getCode())
          .eq(CircleMemberAchievement::getRevoked, false);
        if (this.count(qw) > 0) return;

        CircleMemberAchievement achievement = new CircleMemberAchievement()
                .setCircleId(circleId)
                .setUserId(userId)
                .setAchievementType(type.getCode())
                .setRevoked(false);
        this.save(achievement);

        try {
            notificationService.sendNotification(
                    userId,
                    "ACHIEVEMENT_EARNED",
                    "获得新徽章",
                    "恭喜获得「" + type.getDescription() + "」徽章！"
            );
        } catch (Exception e) {
            log.warn("发送徽章通知失败: userId={}, type={}", userId, type, e);
        }

        // WebSocket 实时推送给该用户
        try {
            com.alibaba.fastjson.JSONObject cmd = new com.alibaba.fastjson.JSONObject();
            cmd.put("type", "ACHIEVEMENT_EARNED");
            cmd.put("circleId", circleId);
            cmd.put("achievementType", type.getCode());
            cmd.put("achievementName", type.getDescription());
            cmd.put("title", "获得新徽章");
            cmd.put("content", "恭喜获得「" + type.getDescription() + "」徽章！");
            sysBaseAPI.sendWebSocketMsg(new String[]{userId}, cmd.toJSONString());
        } catch (Exception e) {
            log.warn("WebSocket推送徽章通知失败: userId={}, type={}", userId, type, e);
        }
    }

    private CircleMemberGrowth getGrowth(String circleId, String userId) {
        LambdaQueryWrapper<CircleMemberGrowth> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleMemberGrowth::getCircleId, circleId)
          .eq(CircleMemberGrowth::getUserId, userId);
        return growthMapper.selectOne(qw);
    }
}
