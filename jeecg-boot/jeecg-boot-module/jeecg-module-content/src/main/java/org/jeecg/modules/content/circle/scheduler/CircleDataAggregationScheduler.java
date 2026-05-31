package org.jeecg.modules.content.circle.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleDataStatistics;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.mapper.CircleDataStatisticsMapper;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class CircleDataAggregationScheduler {

    @Resource
    private CircleDataStatisticsMapper dataMapper;

    @Resource
    private CircleMapper circleMapper;

    @Resource
    private CircleMemberMapper memberMapper;

    @Scheduled(fixedRate = 1800000) // 每30分钟执行一次
    public void aggregateData() {
        log.info("开始执行圈子数据聚合定时任务");
        try {
            LocalDate today = LocalDate.now();

            // 1. 查询所有活跃圈子
            List<Circle> circles = circleMapper.selectList(
                    new LambdaQueryWrapper<Circle>().eq(Circle::getStatus, Circle.Status.ACTIVE));

            for (Circle circle : circles) {
                // 2. 统计成员数
                long memberCount = memberMapper.selectCount(
                        new LambdaQueryWrapper<CircleMember>()
                                .eq(CircleMember::getCircleId, circle.getId())
                                .eq(CircleMember::getStatus, "ACTIVE"));

                // 3. 统计今日新增成员数
                long newMemberCount = memberMapper.selectCount(
                        new LambdaQueryWrapper<CircleMember>()
                                .eq(CircleMember::getCircleId, circle.getId())
                                .eq(CircleMember::getStatus, "ACTIVE")
                                .ge(CircleMember::getCreateTime, today.atStartOfDay()));

                // 4. 构建统计数据
                CircleDataStatistics stats = new CircleDataStatistics();
                stats.setCircleId(circle.getId());
                stats.setStatDate(today);
                stats.setMemberCount((int) memberCount);
                stats.setNewMemberCount((int) newMemberCount);
                stats.setPostCount(0); // TODO: 需要 CircleContentMapper 支持
                stats.setNewPostCount(0);
                stats.setActiveCount(0); // TODO: 需要活跃用户统计逻辑

                // 5. 插入或更新
                CircleDataStatistics existing = dataMapper.selectOne(
                        new LambdaQueryWrapper<CircleDataStatistics>()
                                .eq(CircleDataStatistics::getCircleId, circle.getId())
                                .eq(CircleDataStatistics::getStatDate, today));

                if (existing != null) {
                    stats.setId(existing.getId());
                    dataMapper.updateById(stats);
                } else {
                    dataMapper.insert(stats);
                }
            }

            log.info("圈子数据聚合定时任务执行完成，处理 {} 个圈子", circles.size());
        } catch (Exception e) {
            log.error("圈子数据聚合定时任务执行异常", e);
        }
    }
}
