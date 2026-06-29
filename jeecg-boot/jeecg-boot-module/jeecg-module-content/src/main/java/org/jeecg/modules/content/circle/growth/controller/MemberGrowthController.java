package org.jeecg.modules.content.circle.growth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.growth.biz.ICircleGrowthBiz;
import org.jeecg.modules.content.circle.growth.vo.MemberGrowthVO;
import org.jeecg.modules.content.circle.growth.vo.ParticipationVO;
import org.jeecg.modules.content.circle.util.CircleSecurityUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "成员成长信息")
@RestController
@Validated
@RequestMapping("/api/v1/content/circle/growth/member")
public class MemberGrowthController {

    @Resource
    private ICircleGrowthBiz circleGrowthBiz;

    @Operation(summary = "获取我在圈子的成长信息")
    @GetMapping("/info")
    public Result<MemberGrowthVO> getMyGrowthInfo(
            @RequestParam @NotBlank(message = "圈子ID不能为空") String circleId) {
        String userId = CircleSecurityUtil.getCurrentUserIdOrThrow();
        return Result.OK(circleGrowthBiz.getMyGrowthInfo(circleId, userId));
    }

    @Operation(summary = "获取我的连续参与进度（含近7天每日状态）")
    @GetMapping("/participation")
    public Result<ParticipationVO> getMyParticipationProgress(
            @RequestParam @NotBlank(message = "圈子ID不能为空") String circleId) {
        String userId = CircleSecurityUtil.getCurrentUserIdOrThrow();
        return Result.OK(circleGrowthBiz.getMyParticipationProgress(circleId, userId));
    }
}
