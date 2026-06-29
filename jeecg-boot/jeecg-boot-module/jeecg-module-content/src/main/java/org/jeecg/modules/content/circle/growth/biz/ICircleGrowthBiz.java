package org.jeecg.modules.content.circle.growth.biz;

import org.jeecg.modules.content.circle.growth.enums.GrowthActionEnum;
import org.jeecg.modules.content.circle.growth.vo.*;

import java.util.List;

public interface ICircleGrowthBiz {

    MemberGrowthVO getMyGrowthInfo(String circleId, String userId);

    ParticipationVO getMyParticipationProgress(String circleId, String userId);

    List<AchievementVO> getMyAchievements(String circleId, String userId);

    CircleLevelVO getCircleLevelInfo(String circleId);

    List<LeaderboardEntryVO> getLeaderboard(String circleId, String dimension, String period, String currentUserId);

    void recordGrowthAction(String circleId, String userId, GrowthActionEnum action, String bizId);

    void revokeGrowthAction(String circleId, String userId, GrowthActionEnum action, String bizId);

    void checkCircleMembership(String circleId, String userId);
}
