package org.jeecg.modules.content.user.service;

import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.modules.content.user.vo.ContentFanProfileVO;
import org.jeecg.modules.content.user.vo.ContentFanTrendVO;
import org.jeecg.modules.content.user.vo.ContentRelationUserPageVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 粉丝数据分析服务接口。
 */
public interface IContentFanAnalyticsService {

    /**
     * 分页查询粉丝列表，支持昵称关键词搜索。
     */
    ContentRelationUserPageVO listFans(String userId, String keyword, Long pageNo, Long pageSize);

    /**
     * 查询粉丝趋势数据。
     */
    List<ContentFanTrendVO> getFanTrend(String userId, String period, LocalDate startDate, LocalDate endDate);

    /**
     * 查询粉丝画像分析。
     */
    ContentFanProfileVO getFanProfile(String userId);

    /**
     * 导出粉丝列表为CSV。
     */
    void exportFans(String userId, HttpServletResponse response);
}
