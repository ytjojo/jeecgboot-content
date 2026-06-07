package org.jeecg.modules.content.circle.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.entity.CircleGovernanceLog;
import org.jeecg.modules.content.circle.service.ICircleGovernanceLogService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "圈子治理日志")
@RestController
@RequestMapping("/api/v1/content/circle/governance-log")
public class CircleGovernanceLogController {

    @Resource
    private ICircleGovernanceLogService governanceLogService;

    @Operation(summary = "分页查询治理日志")
    @GetMapping("/list")
    public Result<IPage<CircleGovernanceLog>> list(
            @RequestParam String circleId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {
        LambdaQueryWrapper<CircleGovernanceLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CircleGovernanceLog::getCircleId, circleId)
                .orderByDesc(CircleGovernanceLog::getCreateTime);
        return Result.OK(governanceLogService.page(new Page<>(current, size), wrapper));
    }
}
