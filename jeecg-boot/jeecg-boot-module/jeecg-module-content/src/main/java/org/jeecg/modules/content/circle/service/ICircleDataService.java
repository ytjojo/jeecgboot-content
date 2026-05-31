package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.vo.CircleDataStatisticsVO;

import java.time.LocalDate;

public interface ICircleDataService {
    /**
     * 获取圈子数据统计
     */
    CircleDataStatisticsVO getStatistics(String circleId, LocalDate startDate, LocalDate endDate);

    /**
     * 导出数据统计为CSV
     */
    String exportCsv(String circleId, LocalDate startDate, LocalDate endDate);
}
