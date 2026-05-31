package org.jeecg.modules.content.circle.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.circle.entity.CircleDataStatistics;
import org.jeecg.modules.content.circle.mapper.CircleDataStatisticsMapper;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CircleDataAggregationScheduler {

    @Resource
    private CircleDataStatisticsMapper dataMapper;

    @Resource
    private CircleMemberMapper memberMapper;

    @Scheduled(fixedRate = 1800000) // 每30分钟执行一次
    public void aggregateData() {
        log.info("开始执行圈子数据聚合定时任务");
        try {
            LocalDate today = LocalDate.now();

            // 1. 单条聚合SQL获取所有圈子的成员统计
            List<Map<String, Object>> statsList = memberMapper.selectMemberStatsGroupByCircle(today.atStartOfDay());

            for (Map<String, Object> row : statsList) {
                String circleId = (String) row.get("circle_id");
                long memberCount = ((Number) row.get("member_count")).longValue();
                long newMemberCount = ((Number) row.get("new_member_count")).longValue();

                // 2. 构建统计数据
                CircleDataStatistics stats = new CircleDataStatistics();
                stats.setCircleId(circleId);
                stats.setStatDate(today);
                stats.setMemberCount(Math.toIntExact(memberCount));
                stats.setNewMemberCount(Math.toIntExact(newMemberCount));
                stats.setPostCount(0);
                stats.setNewPostCount(0);
                stats.setActiveCount(0);

                // 3. 插入或更新
                CircleDataStatistics existing = dataMapper.selectOne(
                        new LambdaQueryWrapper<CircleDataStatistics>()
                                .eq(CircleDataStatistics::getCircleId, circleId)
                                .eq(CircleDataStatistics::getStatDate, today));

                if (existing != null) {
                    stats.setId(existing.getId());
                    dataMapper.updateById(stats);
                } else {
                    dataMapper.insert(stats);
                }
            }

            log.info("圈子数据聚合定时任务执行完成，处理 {} 个圈子", statsList.size());
        } catch (Exception e) {
            log.error("圈子数据聚合定时任务执行异常", e);
            throw e;
        }
    }
}
