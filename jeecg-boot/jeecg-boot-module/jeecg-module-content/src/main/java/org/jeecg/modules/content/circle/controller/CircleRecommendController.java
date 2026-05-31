package org.jeecg.modules.content.circle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.circle.service.ICircleRecommendService;
import org.jeecg.modules.content.circle.vo.CircleRecommendVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@Tag(name = "圈子推荐")
@RestController
@RequestMapping("/api/circle")
public class CircleRecommendController {

    @Resource
    private ICircleRecommendService recommendService;

    @Operation(summary = "获取推荐圈子")
    @GetMapping("/recommend")
    public Result<CircleRecommendVO> getRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        String userId = SecureUtil.currentUser().getId();
        return Result.OK(recommendService.getRecommendations(userId, limit));
    }

    @Operation(summary = "记录推荐点击")
    @PostMapping("/recommend/click")
    public Result<String> recordClick(@RequestParam String sourceId) {
        String userId = SecureUtil.currentUser().getId();
        recommendService.recordClick(sourceId, userId);
        return Result.OK("记录成功");
    }

    @Operation(summary = "记录推荐加入转化")
    @PostMapping("/recommend/join")
    public Result<String> recordJoin(@RequestParam String sourceId) {
        String userId = SecureUtil.currentUser().getId();
        recommendService.recordJoin(sourceId, userId);
        return Result.OK("记录成功");
    }
}
