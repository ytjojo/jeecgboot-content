package org.jeecg.modules.content.circle.growth.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.growth.enums.GrowthActionEnum;
import org.jeecg.modules.content.circle.growth.service.IAchievementService;
import org.jeecg.modules.content.circle.growth.service.ICircleLevelService;
import org.jeecg.modules.content.circle.growth.service.ILeaderboardService;
import org.jeecg.modules.content.circle.growth.service.IMemberGrowthService;
import org.jeecg.modules.content.circle.growth.vo.*;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CircleGrowthBizImpl implements ICircleGrowthBiz {

    @Resource
    private IMemberGrowthService memberGrowthService;

    @Resource
    private IAchievementService achievementService;

    @Resource
    private ICircleLevelService circleLevelService;

    @Resource
    private ILeaderboardService leaderboardService;

    @Resource
    private ICircleMemberService circleMemberService;

    @Override
    @Transactional(readOnly = true)
    public MemberGrowthVO getMyGrowthInfo(String circleId, String userId) {
        checkCircleMembership(circleId, userId);
        return memberGrowthService.getGrowthInfo(circleId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipationVO getMyParticipationProgress(String circleId, String userId) {
        checkCircleMembership(circleId, userId);
        return memberGrowthService.getParticipationProgress(circleId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementVO> getMyAchievements(String circleId, String userId) {
        checkCircleMembership(circleId, userId);
        return achievementService.getMemberAchievements(circleId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public CircleLevelVO getCircleLevelInfo(String circleId) {
        return circleLevelService.getLevelInfo(circleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardEntryVO> getLeaderboard(String circleId, String dimension, String period, String currentUserId) {
        if (currentUserId != null) {
            checkCircleMembership(circleId, currentUserId);
        }
        return leaderboardService.getLeaderboard(circleId, dimension, period, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordGrowthAction(String circleId, String userId, GrowthActionEnum action, String bizId) {
        memberGrowthService.addExperience(circleId, userId, action, bizId);
        try {
            achievementService.checkAndAward(circleId, userId);
        } catch (Exception e) {
            log.warn("徽章检查异步执行失败，不影响主流程: circleId={}, userId={}", circleId, userId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeGrowthAction(String circleId, String userId, GrowthActionEnum action, String bizId) {
        memberGrowthService.revokeExperience(circleId, userId, action, bizId);
    }

    @Override
    public void checkCircleMembership(String circleId, String userId) {
        if (userId == null) {
            throw new JeecgBootException("用户未登录");
        }
        long count = circleMemberService.count(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, circleId)
                .eq(CircleMember::getUserId, userId)
                .ne(CircleMember::getStatus, CircleMember.Status.REMOVED));
        if (count == 0) {
            throw new JeecgBootException("您不是该圈子成员，无权访问");
        }
    }
}
