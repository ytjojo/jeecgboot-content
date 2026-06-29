package org.jeecg.modules.content.circle.growth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.growth.biz.ICircleGrowthBiz;
import org.jeecg.modules.content.circle.growth.vo.AchievementVO;
import org.jeecg.modules.content.circle.util.CircleSecurityUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "成就徽章")
@RestController
@Validated
@RequestMapping("/api/v1/content/circle/growth/achievement")
public class AchievementController {

    @Resource
    private ICircleGrowthBiz circleGrowthBiz;

    @Operation(summary = "获取我在圈子的徽章列表")
    @GetMapping("/list")
    public Result<List<AchievementVO>> getMyAchievements(
            @RequestParam @NotBlank(message = "圈子ID不能为空") String circleId) {
        String userId = CircleSecurityUtil.getCurrentUserIdOrThrow();
        return Result.OK(circleGrowthBiz.getMyAchievements(circleId, userId));
    }
}
