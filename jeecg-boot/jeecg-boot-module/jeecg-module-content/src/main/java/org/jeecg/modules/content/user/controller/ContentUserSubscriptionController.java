package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.entity.ContentUserSubscription;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionReq;
import org.jeecg.modules.content.user.service.IContentUserSubscriptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ReST endpoints for content user subscriptions.
 */
@Tag(name = "内容社区用户订阅")
@RestController
@RequestMapping("/content/user/subscription")
public class ContentUserSubscriptionController {

    @Resource
    private IContentUserSubscriptionService subscriptionService;

    /**
     * Creates or resumes a subscription for the target source.
     */
    @Operation(summary = "订阅内容源")
    @PostMapping("/subscribe")
    public Result<String> subscribe(@RequestParam("userId") String userId,
                                    @Valid @RequestBody ContentSubscriptionReq req) {
        return Result.OK(subscriptionService.subscribe(userId, req));
    }

    /**
     * Pauses the specified subscription.
     */
    @Operation(summary = "暂停订阅")
    @PostMapping("/pause")
    public Result<String> pause(@RequestParam("userId") String userId,
                                @RequestParam("subscriptionId") String subscriptionId) {
        subscriptionService.pauseSubscription(userId, subscriptionId);
        return Result.OK("暂停订阅成功");
    }

    /**
     * Resumes the specified subscription.
     */
    @Operation(summary = "恢复订阅")
    @PostMapping("/resume")
    public Result<String> resume(@RequestParam("userId") String userId,
                                 @RequestParam("subscriptionId") String subscriptionId) {
        subscriptionService.resumeSubscription(userId, subscriptionId);
        return Result.OK("恢复订阅成功");
    }

    /**
     * Cancels the specified subscription.
     */
    @Operation(summary = "取消订阅")
    @PostMapping("/cancel")
    public Result<String> cancel(@RequestParam("userId") String userId,
                                 @RequestParam("subscriptionId") String subscriptionId) {
        subscriptionService.cancelSubscription(userId, subscriptionId);
        return Result.OK("取消订阅成功");
    }

    /**
     * Lists all subscriptions owned by the target user.
     */
    @Operation(summary = "查询订阅列表")
    @GetMapping("/list")
    public Result<List<ContentUserSubscription>> list(@RequestParam("userId") String userId) {
        return Result.OK(subscriptionService.listSubscriptions(userId));
    }
}
