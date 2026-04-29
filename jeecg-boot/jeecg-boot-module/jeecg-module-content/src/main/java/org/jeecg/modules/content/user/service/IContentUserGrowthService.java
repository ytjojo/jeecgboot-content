package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.vo.ContentUserGrowthVO;

public interface IContentUserGrowthService {

    void recordBehavior(String userId, String sourceType, int pointDelta, int growthDelta);

    ContentUserGrowthVO getGrowthSummary(String userId);
}
