package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.req.query.ChannelRecommendationQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelRecommendationService;
import org.jeecg.modules.content.channel.vo.ChannelRecommendationVO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道推荐")
@RestController
@RequestMapping("/content/channel/recommendation")
public class ContentChannelRecommendationController {

    @Resource
    private IContentChannelRecommendationService recommendationService;

    @Operation(summary = "获取推荐频道列表")
    @GetMapping("/list")
    public Result<IPage<ChannelRecommendationVO>> getRecommendations(
            @RequestParam String userId,
            ChannelRecommendationQueryReq req) {
        return Result.OK(recommendationService.getRecommendations(userId, req));
    }

    @Operation(summary = "冷启动推荐（无行为数据用户）")
    @GetMapping("/cold-start")
    public Result<IPage<ChannelRecommendationVO>> getColdStartRecommendations(
            ChannelRecommendationQueryReq req) {
        return Result.OK(recommendationService.getColdStartRecommendations(req));
    }

    @Operation(summary = "标记不感兴趣")
    @PostMapping("/not-interested")
    public Result<Void> markNotInterested(
            @RequestParam String userId,
            @RequestParam String channelId) {
        recommendationService.markNotInterested(userId, channelId);
        return Result.OK();
    }
}
