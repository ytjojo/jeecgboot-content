package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.dto.ContentUserRewardEventDTO;
import org.jeecg.modules.content.user.dto.ContentUserRewardResultDTO;
import org.jeecg.modules.content.user.vo.ContentUserGrowthVO;

/**
 * Service contract for content user growth.
 */
public interface IContentUserGrowthService {

    void recordBehavior(String userId, String sourceType, int pointDelta, int growthDelta);

    /**
     * 处理统一奖励事件。
     */
    ContentUserRewardResultDTO reward(ContentUserRewardEventDTO event);

    ContentUserGrowthVO getGrowthSummary(String userId);
}
