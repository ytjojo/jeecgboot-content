package org.jeecg.modules.content.circle.growth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.growth.biz.ICircleGrowthBiz;
import org.jeecg.modules.content.circle.growth.vo.LeaderboardEntryVO;
import org.jeecg.modules.content.circle.util.CircleSecurityUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "圈子成员内排行榜")
@RestController
@Validated
@RequestMapping("/api/v1/content/circle/growth/leaderboard")
public class LeaderboardController {

    @Resource
    private ICircleGrowthBiz circleGrowthBiz;

    @Operation(summary = "获取圈子内成员排行榜（公开接口，登录用户高亮自己）")
    @GetMapping
    public Result<List<LeaderboardEntryVO>> getLeaderboard(
            @RequestParam @NotBlank(message = "圈子ID不能为空") String circleId,
            @RequestParam @NotBlank(message = "维度不能为空") @Pattern(regexp = "EXP|CONTRIBUTION|POST", message = "维度仅支持EXP/CONTRIBUTION/POST") String dimension,
            @RequestParam(defaultValue = "WEEK") @Pattern(regexp = "WEEK|MONTH|ALL", message = "周期仅支持WEEK/MONTH/ALL") String period) {
        String currentUserId = CircleSecurityUtil.getCurrentUserIdOrNull();
        List<LeaderboardEntryVO> result = circleGrowthBiz.getLeaderboard(circleId, dimension, period, currentUserId);
        if (result != null) {
            result.forEach(entry -> {
                if (entry.getUserId() == null) entry.setUserId("");
                if (entry.getUsername() == null) entry.setUsername("匿名用户");
            });
        }
        return Result.OK(result);
    }
}
