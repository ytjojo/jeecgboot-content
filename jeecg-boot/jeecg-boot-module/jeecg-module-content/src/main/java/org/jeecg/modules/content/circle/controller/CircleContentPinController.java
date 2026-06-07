package org.jeecg.modules.content.circle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.content.circle.biz.CircleContentPinBizService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 圈子内容置顶与精华控制器。
 */
@Tag(name = "圈子内容置顶与精华", description = "内容置顶、精华标记管理")
@RestController
@RequestMapping("/api/v1/content/circle/content")
public class CircleContentPinController {

    @Resource
    private CircleContentPinBizService circleContentPinBizService;

    @Operation(summary = "切换置顶状态")
    @PutMapping("/{contentId}/pin")
    public Result<String> togglePin(
            @PathVariable @Parameter(description = "内容ID") String contentId,
            @RequestParam @Parameter(description = "圈子ID") String circleId,
            HttpServletRequest request) {
        String operatorId = JwtUtil.getUserNameByToken(request);
        circleContentPinBizService.pin(contentId, operatorId, circleId);
        return Result.OK("操作成功");
    }

    @Operation(summary = "切换精华状态")
    @PutMapping("/{contentId}/featured")
    public Result<String> toggleFeature(
            @PathVariable @Parameter(description = "内容ID") String contentId,
            @RequestParam @Parameter(description = "圈子ID") String circleId,
            HttpServletRequest request) {
        String operatorId = JwtUtil.getUserNameByToken(request);
        circleContentPinBizService.feature(contentId, operatorId, circleId);
        return Result.OK("操作成功");
    }
}
