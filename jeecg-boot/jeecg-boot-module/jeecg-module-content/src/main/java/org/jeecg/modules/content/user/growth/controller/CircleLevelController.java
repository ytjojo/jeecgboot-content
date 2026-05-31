package org.jeecg.modules.content.user.growth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.growth.service.ICircleLevelService;
import org.jeecg.modules.content.user.growth.vo.CircleLevelVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@Tag(name = "圈子等级信息")
@RestController
@RequestMapping("/content/user/growth/level")
public class CircleLevelController {

    @Resource
    private ICircleLevelService circleLevelService;

    @Operation(summary = "获取圈子等级信息")
    @GetMapping("/info")
    public Result<CircleLevelVO> getLevelInfo(@RequestParam String circleId) {
        return Result.OK(circleLevelService.getLevelInfo(circleId));
    }
}
