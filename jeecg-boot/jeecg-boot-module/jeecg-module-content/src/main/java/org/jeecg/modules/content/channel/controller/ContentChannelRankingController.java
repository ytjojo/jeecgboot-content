package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelRankingService;
import org.jeecg.modules.content.channel.vo.ChannelRankingItemVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "频道排行榜")
@RestController
@RequestMapping("/content/channel/ranking")
public class ContentChannelRankingController {

    @Resource
    private IContentChannelRankingService rankingService;

    @Operation(summary = "获取热门频道榜")
    @GetMapping("/hot")
    public Result<List<ChannelRankingItemVO>> getHotRanking(ChannelRankingQueryReq req) {
        return Result.OK(rankingService.getHotRanking(req));
    }

    @Operation(summary = "获取新晋频道榜")
    @GetMapping("/new")
    public Result<List<ChannelRankingItemVO>> getNewRanking(ChannelRankingQueryReq req) {
        return Result.OK(rankingService.getNewRanking(req));
    }

    @Operation(summary = "获取系统频道榜")
    @GetMapping("/system")
    public Result<List<ChannelRankingItemVO>> getSystemRanking(ChannelRankingQueryReq req) {
        return Result.OK(rankingService.getSystemRanking(req));
    }
}
