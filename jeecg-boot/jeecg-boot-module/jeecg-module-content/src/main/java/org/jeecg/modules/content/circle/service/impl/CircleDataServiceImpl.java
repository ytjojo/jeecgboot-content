package org.jeecg.modules.content.circle.service.impl;

import org.jeecg.modules.content.circle.entity.CircleDataStatistics;
import org.jeecg.modules.content.circle.mapper.CircleDataStatisticsMapper;
import org.jeecg.modules.content.circle.service.ICircleDataService;
import org.jeecg.modules.content.circle.vo.CircleDataStatisticsVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CircleDataServiceImpl implements ICircleDataService {

    @Resource
    private CircleDataStatisticsMapper dataMapper;

    @Override
    public CircleDataStatisticsVO getStatistics(String circleId, LocalDate startDate, LocalDate endDate) {
        List<CircleDataStatistics> stats = dataMapper.selectByCircleIdAndDateRange(circleId, startDate, endDate);

        CircleDataStatisticsVO vo = new CircleDataStatisticsVO();
        if (stats.isEmpty()) {
            vo.setMemberCount(0);
            vo.setNewMemberCount(0);
            vo.setPostCount(0);
            vo.setNewPostCount(0);
            vo.setActiveCount(0);
            vo.setDailyTrends(new ArrayList<>());
            return vo;
        }

        // 取最新一天的数据作为总数
        CircleDataStatistics latest = stats.get(stats.size() - 1);
        vo.setMemberCount(latest.getMemberCount());
        vo.setPostCount(latest.getPostCount());
        vo.setActiveCount(latest.getActiveCount());

        // 汇总新增数据
        vo.setNewMemberCount(stats.stream().mapToInt(CircleDataStatistics::getNewMemberCount).sum());
        vo.setNewPostCount(stats.stream().mapToInt(CircleDataStatistics::getNewPostCount).sum());

        // 构建每日趋势
        List<CircleDataStatisticsVO.DailyTrend> trends = stats.stream()
                .map(s -> {
                    CircleDataStatisticsVO.DailyTrend trend = new CircleDataStatisticsVO.DailyTrend();
                    trend.setDate(s.getStatDate());
                    trend.setNewMemberCount(s.getNewMemberCount());
                    trend.setNewPostCount(s.getNewPostCount());
                    trend.setActiveCount(s.getActiveCount());
                    return trend;
                })
                .collect(Collectors.toList());
        vo.setDailyTrends(trends);

        return vo;
    }

    @Override
    public String exportCsv(String circleId, LocalDate startDate, LocalDate endDate) {
        List<CircleDataStatistics> stats = dataMapper.selectByCircleIdAndDateRange(circleId, startDate, endDate);

        StringBuilder csv = new StringBuilder();
        csv.append("日期,成员总数,新增成员数,帖子总数,新增帖子数,活跃用户数\n");

        for (CircleDataStatistics s : stats) {
            csv.append(String.format("%s,%d,%d,%d,%d,%d\n",
                    s.getStatDate(),
                    s.getMemberCount(),
                    s.getNewMemberCount(),
                    s.getPostCount(),
                    s.getNewPostCount(),
                    s.getActiveCount()));
        }

        return csv.toString();
    }
}
