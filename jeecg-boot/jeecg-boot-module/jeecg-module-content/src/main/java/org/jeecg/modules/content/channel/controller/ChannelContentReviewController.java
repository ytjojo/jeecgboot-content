package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.channel.biz.ChannelReviewBiz;
import org.jeecg.modules.content.channel.req.review.ChannelReviewReq;
import org.jeecg.modules.content.channel.vo.review.ReviewStatsVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道内容审核", description = "频道待审区和审核相关接口")
@Validated
@RestController
@RequestMapping("/api/v1/content/channel/review")
public class ChannelContentReviewController {

    @Resource
    private ChannelReviewBiz channelReviewBiz;

    @Operation(summary = "审核内容")
    @PostMapping
    public Result<Void> review(@Valid @RequestBody ChannelReviewReq req) {
        String userId = SecureUtil.currentUser().getId();
        channelReviewBiz.review(req, userId);
        return Result.OK();
    }

    @Operation(summary = "获取审核统计")
    @GetMapping("/stats")
    public Result<ReviewStatsVO> getReviewStats(@NotBlank(message = "频道ID不能为空") @RequestParam String channelId) {
        return Result.OK(channelReviewBiz.getReviewStats(channelId));
    }
}
