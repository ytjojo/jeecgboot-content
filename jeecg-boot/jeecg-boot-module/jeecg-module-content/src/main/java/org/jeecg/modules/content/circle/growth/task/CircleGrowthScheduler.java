package org.jeecg.modules.content.circle.growth.task;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.circle.growth.entity.CircleLevel;
import org.jeecg.modules.content.circle.growth.mapper.CircleLevelMapper;
import org.jeecg.modules.content.circle.growth.service.ICircleLevelService;
import org.jeecg.modules.content.circle.growth.service.ILeaderboardService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class CircleGrowthScheduler {

    @Resource
    private ICircleLevelService circleLevelService;
    @Resource
    private ILeaderboardService leaderboardService;
    @Resource
    private CircleLevelMapper levelMapper;

    @Scheduled(fixedDelayString = "${content.circle.growth.level-update.fixed-delay-ms:1800000}")
    public void updateCircleLevels() {
        log.info("开始更新圈子等级");
        List<CircleLevel> levels = levelMapper.selectList(null);
        for (CircleLevel level : levels) {
            try {
                circleLevelService.recalculateAndUpdateLevel(level.getCircleId());
            } catch (Exception e) {
                log.error("更新圈子{}等级失败", level.getCircleId(), e);
            }
        }
        log.info("圈子等级更新完成，共{}个圈子", levels.size());
    }

    @Scheduled(fixedDelayString = "${content.circle.growth.leaderboard.fixed-delay-ms:3600000}")
    public void refreshLeaderboards() {
        log.info("开始刷新排行榜");
        List<CircleLevel> levels = levelMapper.selectList(null);
        for (CircleLevel level : levels) {
            try {
                leaderboardService.refreshSnapshot(level.getCircleId());
            } catch (Exception e) {
                log.error("刷新圈子{}排行榜失败", level.getCircleId(), e);
            }
        }
        log.info("排行榜刷新完成，共{}个圈子", levels.size());
    }
}
