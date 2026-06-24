package org.jeecg.modules.content.user.growth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.growth.service.ILeaderboardService;
import org.jeecg.modules.content.user.growth.vo.LeaderboardEntryVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "圈子排行榜")
@RestController
@RequestMapping("/api/v1/content/circle/growth/leaderboard")
public class LeaderboardController {

    @Resource
    private ILeaderboardService leaderboardService;

    @Operation(summary = "获取圈子排行榜")
    @GetMapping
    public Result<List<LeaderboardEntryVO>> getLeaderboard(
            @RequestParam String circleId,
            @RequestParam String dimension,
            @RequestParam(defaultValue = "WEEK") String period,
            @RequestParam String currentUserId) {
        return Result.OK(leaderboardService.getLeaderboard(circleId, dimension, period, currentUserId));
    }
}
