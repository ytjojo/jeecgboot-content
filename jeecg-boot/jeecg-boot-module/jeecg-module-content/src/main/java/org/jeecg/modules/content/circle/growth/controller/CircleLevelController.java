package org.jeecg.modules.content.circle.growth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.growth.biz.ICircleGrowthBiz;
import org.jeecg.modules.content.circle.growth.vo.CircleLevelVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "圈子等级信息")
@RestController
@Validated
@RequestMapping("/api/v1/content/circle/growth/level")
public class CircleLevelController {

    @Resource
    private ICircleGrowthBiz circleGrowthBiz;

    @Operation(summary = "获取圈子等级信息（公开接口）")
    @GetMapping("/info")
    public Result<CircleLevelVO> getLevelInfo(
            @RequestParam @NotBlank(message = "圈子ID不能为空") String circleId) {
        return Result.OK(circleGrowthBiz.getCircleLevelInfo(circleId));
    }
}
