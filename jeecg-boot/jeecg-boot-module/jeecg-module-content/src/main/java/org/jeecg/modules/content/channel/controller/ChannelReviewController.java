package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.req.ChannelReviewActionReq;
import org.jeecg.modules.content.channel.service.IChannelReviewService;
import org.jeecg.modules.content.channel.vo.ChannelReviewVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/jeecg-boot/api/v1/content/channel/review")
@Tag(name = "频道审核", description = "频道审核管理接口")
public class ChannelReviewController {

    private static final Set<String> VALID_ACTIONS = Set.of("approved", "rejected", "returned");

    @Resource
    private IChannelReviewService reviewService;

    @GetMapping("/list")
    @Operation(summary = "审核队列列表")
    public Result<Page<ChannelReviewVO>> listReviews(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String reviewType) {

        LambdaQueryWrapper<ChannelReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, ChannelReview::getStatus, status);
        wrapper.eq(reviewType != null, ChannelReview::getReviewType, reviewType);
        wrapper.orderByDesc(ChannelReview::getSubmitTime);

        Page<ChannelReview> page = reviewService.page(new Page<>(current, size), wrapper);
        Page<ChannelReviewVO> voPage = (Page<ChannelReviewVO>) page.convert(this::convertToVO);
        return Result.OK(voPage);
    }

    @PostMapping("/action")
    @Operation(summary = "审核操作")
    public Result<Void> reviewAction(@Valid @RequestBody ChannelReviewActionReq req) {
        if (!VALID_ACTIONS.contains(req.getAction())) {
            return Result.error("无效的审核操作: " + req.getAction());
        }

        ChannelReview review = reviewService.getById(req.getReviewId());
        if (review == null) {
            return Result.error("审核记录不存在");
        }

        review.setStatus(req.getAction());
        review.setReviewerId(getCurrentUserId());
        review.setReviewReason(req.getReason());
        review.setReviewTime(LocalDateTime.now());
        reviewService.updateById(review);

        return Result.OK();
    }

    private ChannelReviewVO convertToVO(ChannelReview review) {
        return ChannelReviewVO.builder()
                .reviewId(review.getReviewId())
                .channelId(review.getChannelId())
                .reviewType(review.getReviewType())
                .status(review.getStatus())
                .submitTime(review.getSubmitTime())
                .timeoutFlag(review.getTimeoutFlag())
                .build();
    }

    private String getCurrentUserId() {
        // TODO: 从安全上下文获取当前用户ID
        return "current-user-id";
    }
}
