package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.vo.CircleRankingVO;

public interface ICircleRankingService {
    /**
     * 获取热门圈子榜单
     */
    CircleRankingVO getHotRanking(int limit);

    /**
     * 获取新增圈子榜单
     */
    CircleRankingVO getNewRanking(int limit);
}
