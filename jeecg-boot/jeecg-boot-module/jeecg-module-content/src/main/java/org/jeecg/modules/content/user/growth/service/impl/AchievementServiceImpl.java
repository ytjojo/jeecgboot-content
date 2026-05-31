package org.jeecg.modules.content.user.growth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.user.growth.entity.CircleAchievement;
import org.jeecg.modules.content.user.growth.entity.CircleMemberAchievement;
import org.jeecg.modules.content.user.growth.entity.CircleMemberGrowth;
import org.jeecg.modules.content.user.growth.enums.AchievementTypeEnum;
import org.jeecg.modules.content.user.growth.mapper.CircleAchievementMapper;
import org.jeecg.modules.content.user.growth.mapper.CircleMemberAchievementMapper;
import org.jeecg.modules.content.user.growth.mapper.CircleMemberGrowthMapper;
import org.jeecg.modules.content.user.growth.service.IAchievementService;
import org.jeecg.modules.content.user.growth.service.IMemberGrowthService;
import org.jeecg.modules.content.user.growth.vo.AchievementVO;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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
    private IMemberGrowthService memberGrowthService;
    @Resource
    private IContentNotificationService notificationService;

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

        List<AchievementVO> result = new ArrayList<>();
        for (CircleAchievement a : allAchievements) {
            AchievementVO vo = new AchievementVO();
            vo.setAchievementType(a.getAchievementType());
            vo.setName(a.getName());
            vo.setDescription(a.getDescription());
            vo.setConditionDesc(a.getConditionDesc());
            vo.setEarned(earnedTypes.contains(a.getAchievementType()));
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
    }

    private CircleMemberGrowth getGrowth(String circleId, String userId) {
        LambdaQueryWrapper<CircleMemberGrowth> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleMemberGrowth::getCircleId, circleId)
          .eq(CircleMemberGrowth::getUserId, userId);
        return growthMapper.selectOne(qw);
    }
}
