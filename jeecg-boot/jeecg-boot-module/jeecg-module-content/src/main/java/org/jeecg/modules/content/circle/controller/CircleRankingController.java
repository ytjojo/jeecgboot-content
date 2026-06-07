package org.jeecg.modules.content.circle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.service.ICircleRankingService;
import org.jeecg.modules.content.circle.vo.CircleRankingVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@Tag(name = "圈子榜单")
@RestController
@RequestMapping("/api/v1/content/circle/ranking")
public class CircleRankingController {

    @Resource
    private ICircleRankingService rankingService;

    @Operation(summary = "获取热门圈子榜单")
    @GetMapping("/hot")
    public Result<CircleRankingVO> getHotRanking(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.OK(rankingService.getHotRanking(limit));
    }

    @Operation(summary = "获取新增圈子榜单")
    @GetMapping("/new")
    public Result<CircleRankingVO> getNewRanking(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.OK(rankingService.getNewRanking(limit));
    }
}
