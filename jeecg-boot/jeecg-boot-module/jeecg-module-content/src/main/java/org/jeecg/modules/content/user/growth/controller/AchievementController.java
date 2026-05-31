package org.jeecg.modules.content.user.growth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.growth.service.IAchievementService;
import org.jeecg.modules.content.user.growth.vo.AchievementVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "成就徽章")
@RestController
@RequestMapping("/content/user/growth/achievement")
public class AchievementController {

    @Resource
    private IAchievementService achievementService;

    @Operation(summary = "获取成员在圈子的徽章列表")
    @GetMapping("/list")
    public Result<List<AchievementVO>> getAchievements(
            @RequestParam String circleId,
            @RequestParam String userId) {
        return Result.OK(achievementService.getMemberAchievements(circleId, userId));
    }
}
