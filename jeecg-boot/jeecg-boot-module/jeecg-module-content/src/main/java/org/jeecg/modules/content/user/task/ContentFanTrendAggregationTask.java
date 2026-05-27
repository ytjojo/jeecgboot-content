package org.jeecg.modules.content.user.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.jeecg.modules.content.user.entity.ContentFanTrendDaily;
import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.mapper.ContentFanTrendDailyMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定时任务：每日聚合新增粉丝数据。
 */
@Component
public class ContentFanTrendAggregationTask {

    @Resource
    private ContentUserRelationMapper relationMapper;

    @Resource
    private ContentFanTrendDailyMapper fanTrendDailyMapper;

    /**
     * 每天凌晨2点执行，统计前一天的新增粉丝数。
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void aggregateDailyFanTrend() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date startOfDay = java.sql.Date.valueOf(yesterday);
        Date endOfDay = java.sql.Date.valueOf(yesterday.plusDays(1));

        List<ContentUserRelation> newFollowers = relationMapper.selectList(
            Wrappers.<ContentUserRelation>lambdaQuery()
                .eq(ContentUserRelation::getFollowed, Boolean.TRUE)
                .ge(ContentUserRelation::getFollowedAt, startOfDay)
                .lt(ContentUserRelation::getFollowedAt, endOfDay));

        Map<String, Long> countByTargetUser = newFollowers.stream()
            .collect(Collectors.groupingBy(ContentUserRelation::getTargetUserId, Collectors.counting()));

        for (Map.Entry<String, Long> entry : countByTargetUser.entrySet()) {
            ContentFanTrendDaily existing = fanTrendDailyMapper.selectOne(
                Wrappers.<ContentFanTrendDaily>lambdaQuery()
                    .eq(ContentFanTrendDaily::getUserId, entry.getKey())
                    .eq(ContentFanTrendDaily::getDate, yesterday));

            if (existing != null) {
                existing.setNewFollowerCount(entry.getValue().intValue());
                fanTrendDailyMapper.updateById(existing);
            } else {
                ContentFanTrendDaily record = new ContentFanTrendDaily()
                    .setUserId(entry.getKey())
                    .setDate(yesterday)
                    .setNewFollowerCount(entry.getValue().intValue());
                fanTrendDailyMapper.insert(record);
            }
        }
    }
}
