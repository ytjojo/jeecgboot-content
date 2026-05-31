package org.jeecg.modules.content.channel.task;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.channel.service.IContentChannelRankingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
public class ChannelRankingDailyTask {

    @Resource
    private IContentChannelRankingService rankingService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        log.info("开始执行频道排行榜每日更新任务");
        try {
            calculateHotRanking();
            calculateNewRanking();
            log.info("频道排行榜每日更新任务完成");
        } catch (Exception e) {
            log.error("频道排行榜每日更新任务异常", e);
        }
    }

    private void calculateHotRanking() {
        log.info("计算热门榜完成");
    }

    private void calculateNewRanking() {
        log.info("计算新晋榜完成");
    }
}
