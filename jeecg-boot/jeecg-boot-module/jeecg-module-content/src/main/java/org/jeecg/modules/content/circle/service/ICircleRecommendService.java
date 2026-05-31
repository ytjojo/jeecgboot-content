package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.vo.CircleRecommendVO;

public interface ICircleRecommendService {
    /**
     * 获取推荐圈子
     */
    CircleRecommendVO getRecommendations(String userId, int limit);

    /**
     * 记录推荐点击
     */
    void recordClick(String sourceId, String userId);

    /**
     * 记录推荐加入转化
     */
    void recordJoin(String sourceId, String userId);
}
