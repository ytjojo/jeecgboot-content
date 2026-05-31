package org.jeecg.modules.content.circle.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CircleRankingScheduler {

    @Resource
    private CircleMapper circleMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String HOT_RANKING_KEY = "circle:ranking:hot";
    private static final String NEW_RANKING_KEY = "circle:ranking:new";

    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void refreshRanking() {
        log.info("开始执行圈子榜单刷新定时任务");
        try {
            // 刷新热门榜单
            var hotCircles = circleMapper.selectHotCircles(20);
            redisTemplate.opsForValue().set(HOT_RANKING_KEY, hotCircles, 2, TimeUnit.HOURS);

            // 刷新新增榜单
            var newCircles = circleMapper.selectNewCircles(20);
            redisTemplate.opsForValue().set(NEW_RANKING_KEY, newCircles, 2, TimeUnit.HOURS);

            log.info("圈子榜单刷新定时任务执行完成");
        } catch (Exception e) {
            log.error("圈子榜单刷新定时任务执行异常", e);
        }
    }
}
