package org.jeecg.modules.content.user.growth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.user.growth.constant.GrowthConstant;
import org.jeecg.modules.content.user.growth.entity.CircleLevel;
import org.jeecg.modules.content.user.growth.entity.CircleMemberGrowth;
import org.jeecg.modules.content.user.growth.enums.CircleLevelEnum;
import org.jeecg.modules.content.user.growth.mapper.CircleLevelMapper;
import org.jeecg.modules.content.user.growth.mapper.CircleMemberGrowthMapper;
import org.jeecg.modules.content.user.growth.service.ICircleLevelService;
import org.jeecg.modules.content.user.growth.vo.CircleLevelVO;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class CircleLevelServiceImpl extends ServiceImpl<CircleLevelMapper, CircleLevel>
        implements ICircleLevelService {

    @Resource
    private CircleMemberGrowthMapper growthMapper;
    @Resource
    private IContentNotificationService notificationService;

    @Override
    public void calculateGrowthScore(String circleId) {
        List<CircleMemberGrowth> members = loadCircleMembers(circleId);
        int memberScore = calculateMemberScore(members);
        int contentScore = calculateContentScore(members);
        int activityScore = calculateActivityScore(members);
        int total = Math.min(memberScore + contentScore + activityScore, GrowthConstant.MAX_GROWTH_SCORE);

        CircleLevel level = getOrCreateLevel(circleId);
        level.setMemberScore(memberScore)
             .setContentScore(contentScore)
             .setActivityScore(activityScore)
             .setGrowthScore(total);
        this.saveOrUpdate(level);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLevel(String circleId) {
        CircleLevel level = getOrCreateLevel(circleId);
        CircleLevelEnum newLevelEnum = CircleLevelEnum.ofScore(level.getGrowthScore());
        int oldLevel = level.getLevel();

        if (newLevelEnum.getLevel() > oldLevel) {
            level.setLevel(newLevelEnum.getLevel());
            this.updateById(level);
            notifyLevelUpgrade(circleId, newLevelEnum);
            log.info("圈子{}等级从L{}提升为L{}", circleId, oldLevel, newLevelEnum.getLevel());
        }
    }

    @Override
    public CircleLevelVO getLevelInfo(String circleId) {
        CircleLevel level = getOrCreateLevel(circleId);
        CircleLevelEnum currentEnum = CircleLevelEnum.ofScore(level.getGrowthScore());

        CircleLevelVO vo = new CircleLevelVO();
        vo.setLevel(level.getLevel());
        vo.setLevelName(currentEnum.getName());
        vo.setGrowthScore(level.getGrowthScore());

        CircleLevelEnum[] values = CircleLevelEnum.values();
        int nextThreshold = GrowthConstant.MAX_GROWTH_SCORE;
        if (currentEnum.getLevel() < 5) {
            nextThreshold = values[currentEnum.getLevel()].getThreshold();
        }
        vo.setNextLevelThreshold(nextThreshold);

        int prevThreshold = currentEnum.getThreshold();
        int range = nextThreshold - prevThreshold;
        int progress = range > 0 ? (level.getGrowthScore() - prevThreshold) * 100 / range : 100;
        vo.setProgressPercent(Math.min(progress, 100));

        return vo;
    }

    private List<CircleMemberGrowth> loadCircleMembers(String circleId) {
        LambdaQueryWrapper<CircleMemberGrowth> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleMemberGrowth::getCircleId, circleId);
        return growthMapper.selectList(qw);
    }

    private int calculateMemberScore(List<CircleMemberGrowth> members) {
        return (int) Math.min(members.size() * 2L, 400);
    }

    private int calculateContentScore(List<CircleMemberGrowth> members) {
        int totalPosts = 0;
        int totalFeatured = 0;
        for (CircleMemberGrowth g : members) {
            totalPosts += g.getPostCount() != null ? g.getPostCount() : 0;
            totalFeatured += g.getFeaturedCount() != null ? g.getFeaturedCount() : 0;
        }
        return (int) Math.min(totalPosts * 2L + totalFeatured * 5L, 300);
    }

    private int calculateActivityScore(List<CircleMemberGrowth> members) {
        int totalComments = 0;
        for (CircleMemberGrowth g : members) {
            totalComments += g.getCommentCount() != null ? g.getCommentCount() : 0;
        }
        return (int) Math.min(totalComments, 300);
    }

    private CircleLevel getOrCreateLevel(String circleId) {
        LambdaQueryWrapper<CircleLevel> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleLevel::getCircleId, circleId);
        CircleLevel level = this.getOne(qw);
        if (level == null) {
            level = new CircleLevel()
                    .setCircleId(circleId)
                    .setLevel(1)
                    .setGrowthScore(0)
                    .setMemberScore(0)
                    .setContentScore(0)
                    .setActivityScore(0);
        }
        return level;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateAndUpdateLevel(String circleId) {
        CircleLevel level = getOrCreateLevel(circleId);
        List<CircleMemberGrowth> members = loadCircleMembers(circleId);
        int memberScore = calculateMemberScore(members);
        int contentScore = calculateContentScore(members);
        int activityScore = calculateActivityScore(members);
        int total = Math.min(memberScore + contentScore + activityScore, GrowthConstant.MAX_GROWTH_SCORE);

        level.setMemberScore(memberScore)
             .setContentScore(contentScore)
             .setActivityScore(activityScore)
             .setGrowthScore(total);

        CircleLevelEnum newLevelEnum = CircleLevelEnum.ofScore(total);
        int oldLevel = level.getLevel();
        if (newLevelEnum.getLevel() > oldLevel) {
            level.setLevel(newLevelEnum.getLevel());
            notifyLevelUpgrade(circleId, newLevelEnum);
            log.info("圈子{}等级从L{}提升为L{}", circleId, oldLevel, newLevelEnum.getLevel());
        }
        this.saveOrUpdate(level);
    }

    private void notifyLevelUpgrade(String circleId, CircleLevelEnum newLevel) {
        try {
            notificationService.sendNotification(
                    circleId,
                    "CIRCLE_LEVEL_UP",
                    "圈子等级提升",
                    "恭喜！您的圈子已提升至" + newLevel.getName()
            );
        } catch (Exception e) {
            log.warn("发送等级提升通知失败: circleId={}", circleId, e);
        }
    }
}
