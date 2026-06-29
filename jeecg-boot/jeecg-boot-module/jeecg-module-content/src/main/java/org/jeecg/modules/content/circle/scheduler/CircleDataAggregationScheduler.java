package org.jeecg.modules.content.circle.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.circle.entity.CircleDataStatistics;
import org.jeecg.modules.content.circle.mapper.CircleContentMapper;
import org.jeecg.modules.content.circle.mapper.CircleDataStatisticsMapper;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CircleDataAggregationScheduler {

    @Resource
    private CircleDataStatisticsMapper dataMapper;

    @Resource
    private CircleMemberMapper memberMapper;

    @Resource
    private CircleContentMapper contentMapper;

    @Scheduled(fixedRate = 1800000)
    public void aggregateData() {
        log.info("开始执行圈子数据聚合定时任务");
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime todayStart = today.atStartOfDay();
            LocalDateTime thirtyDaysAgo = todayStart.minusDays(30);

            List<Map<String, Object>> memberStats = memberMapper.selectMemberStatsGroupByCircle(todayStart);
            List<Map<String, Object>> postStats = contentMapper.selectPostStatsGroupByCircle(todayStart);
            List<Map<String, Object>> activeStats = contentMapper.selectActiveUserStatsGroupByCircle(thirtyDaysAgo);

            Map<String, long[]> mergedStats = new HashMap<>();

            for (Map<String, Object> row : memberStats) {
                String circleId = (String) row.get("circle_id");
                long memberCount = ((Number) row.get("member_count")).longValue();
                long newMemberCount = ((Number) row.get("new_member_count")).longValue();
                mergedStats.computeIfAbsent(circleId, k -> new long[5]);
                mergedStats.get(circleId)[0] = memberCount;
                mergedStats.get(circleId)[1] = newMemberCount;
            }

            for (Map<String, Object> row : postStats) {
                String circleId = (String) row.get("circle_id");
                long postCount = row.get("post_count") != null ? ((Number) row.get("post_count")).longValue() : 0;
                long newPostCount = row.get("new_post_count") != null ? ((Number) row.get("new_post_count")).longValue() : 0;
                mergedStats.computeIfAbsent(circleId, k -> new long[5]);
                mergedStats.get(circleId)[2] = postCount;
                mergedStats.get(circleId)[3] = newPostCount;
            }

            for (Map<String, Object> row : activeStats) {
                String circleId = (String) row.get("circle_id");
                long activeCount = row.get("active_count") != null ? ((Number) row.get("active_count")).longValue() : 0;
                mergedStats.computeIfAbsent(circleId, k -> new long[5]);
                mergedStats.get(circleId)[4] = activeCount;
            }

            for (Map.Entry<String, long[]> entry : mergedStats.entrySet()) {
                String circleId = entry.getKey();
                long[] values = entry.getValue();

                CircleDataStatistics stats = new CircleDataStatistics();
                stats.setCircleId(circleId);
                stats.setStatDate(today);
                stats.setMemberCount(Math.toIntExact(values[0]));
                stats.setNewMemberCount(Math.toIntExact(values[1]));
                stats.setPostCount(Math.toIntExact(values[2]));
                stats.setNewPostCount(Math.toIntExact(values[3]));
                stats.setActiveCount(Math.toIntExact(values[4]));

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

            log.info("圈子数据聚合定时任务执行完成，处理 {} 个圈子", mergedStats.size());
        } catch (Exception e) {
            log.error("圈子数据聚合定时任务执行异常", e);
        }
    }
}
