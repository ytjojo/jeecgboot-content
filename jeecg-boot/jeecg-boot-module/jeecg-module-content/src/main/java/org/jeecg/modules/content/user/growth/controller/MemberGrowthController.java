package org.jeecg.modules.content.user.growth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.growth.service.IMemberGrowthService;
import org.jeecg.modules.content.user.growth.vo.MemberGrowthVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@Tag(name = "成员成长信息")
@RestController
@RequestMapping("/api/v1/content/user/growth")
public class MemberGrowthController {

    @Resource
    private IMemberGrowthService memberGrowthService;

    @Operation(summary = "获取成员在圈子的成长信息")
    @GetMapping("/info")
    public Result<MemberGrowthVO> getGrowthInfo(
            @RequestParam String circleId,
            @RequestParam String userId) {
        return Result.OK(memberGrowthService.getGrowthInfo(circleId, userId));
    }

    @Operation(summary = "获取连续参与进度")
    @GetMapping("/participation")
    public Result<Integer> getParticipationDays(
            @RequestParam String circleId,
            @RequestParam String userId) {
        return Result.OK(memberGrowthService.getParticipationDays(circleId, userId));
    }
}
