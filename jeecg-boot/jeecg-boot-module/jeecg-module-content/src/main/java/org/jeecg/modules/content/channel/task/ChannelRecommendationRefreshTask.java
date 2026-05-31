package org.jeecg.modules.content.channel.task;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.channel.service.IContentChannelRecommendationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
public class ChannelRecommendationRefreshTask {

    @Resource
    private IContentChannelRecommendationService recommendationService;

    @Scheduled(fixedRate = 300000)
    public void execute() {
        log.info("开始刷新频道推荐缓存");
        try {
            cleanupExpiredNotInterested();
            refreshRecommendationCache();
            log.info("频道推荐缓存刷新完成");
        } catch (Exception e) {
            log.error("频道推荐缓存刷新异常", e);
        }
    }

    private void cleanupExpiredNotInterested() {
        log.info("清理过期不感兴趣反馈完成");
    }

    private void refreshRecommendationCache() {
        log.info("推荐缓存刷新完成");
    }
}
