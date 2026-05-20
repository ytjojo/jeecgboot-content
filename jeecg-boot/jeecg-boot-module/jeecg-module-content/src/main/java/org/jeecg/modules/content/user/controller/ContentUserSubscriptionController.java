package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionBatchReq;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionNotificationPreferenceReq;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionReq;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionSourceReq;
import org.jeecg.modules.content.user.service.IContentSubscriptionNotificationPreferenceService;
import org.jeecg.modules.content.user.service.IContentSubscriptionSourceService;
import org.jeecg.modules.content.user.service.IContentUserSubscriptionService;
import org.jeecg.modules.content.user.vo.ContentSubscriptionBatchResultVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionFeedPageVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionNotificationDecisionVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionNotificationPreferenceVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionSourceDetailVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionSourcePageVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionSourceVO;
import org.jeecg.modules.content.user.vo.ContentUserSubscriptionPageVO;
import org.jeecg.modules.content.user.vo.ContentUserSubscriptionVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ReST endpoints for content user subscriptions.
 */
@Tag(name = "内容社区用户订阅")
@RestController
@RequestMapping("/content/user/subscription")
public class ContentUserSubscriptionController {

    @Resource
    private IContentUserSubscriptionService subscriptionService;

    @Resource
    private IContentSubscriptionNotificationPreferenceService notificationPreferenceService;

    @Resource
    private IContentSubscriptionSourceService sourceService;

    /**
     * Creates or resumes a subscription for the target source.
     */
    @Operation(summary = "订阅内容源")
    @PostMapping("/subscribe")
    public Result<ContentUserSubscriptionVO> subscribe(@RequestParam("userId") String userId,
                                                       @Valid @RequestBody ContentSubscriptionReq req) {
        return Result.OK(subscriptionService.subscribe(userId, req));
    }

    /**
     * Pauses the specified subscription.
     */
    @Operation(summary = "暂停订阅")
    @PostMapping("/pause")
    public Result<ContentUserSubscriptionVO> pause(@RequestParam("userId") String userId,
                                                   @RequestParam("subscriptionId") String subscriptionId) {
        return Result.OK(subscriptionService.pauseSubscription(userId, subscriptionId));
    }

    /**
     * Resumes the specified subscription.
     */
    @Operation(summary = "恢复订阅")
    @PostMapping("/resume")
    public Result<ContentUserSubscriptionVO> resume(@RequestParam("userId") String userId,
                                                    @RequestParam("subscriptionId") String subscriptionId) {
        return Result.OK(subscriptionService.resumeSubscription(userId, subscriptionId));
    }

    /**
     * Cancels the specified subscription.
     */
    @Operation(summary = "取消订阅")
    @PostMapping("/cancel")
    public Result<ContentUserSubscriptionVO> cancel(@RequestParam("userId") String userId,
                                                    @RequestParam("subscriptionId") String subscriptionId) {
        return Result.OK(subscriptionService.cancelSubscription(userId, subscriptionId));
    }

    /**
     * Lists all subscriptions owned by the target user.
     */
    @Operation(summary = "查询订阅列表")
    @GetMapping("/list")
    public Result<ContentUserSubscriptionPageVO> list(@RequestParam("userId") String userId,
                                                      @RequestParam(value = "sourceType", required = false) String sourceType,
                                                      @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
                                                      @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(subscriptionService.listSubscriptions(userId, sourceType, pageNo, pageSize));
    }

    /**
     * 查询订阅源更新流。
     */
    @Operation(summary = "查询订阅流")
    @GetMapping("/feed")
    public Result<ContentSubscriptionFeedPageVO> feed(@RequestParam("userId") String userId,
                                                      @RequestParam(value = "sourceType", required = false) String sourceType,
                                                      @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
                                                      @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(subscriptionService.listSubscriptionFeed(userId, sourceType, pageNo, pageSize));
    }

    /**
     * 写入订阅源目录。
     */
    @Operation(summary = "写入订阅源目录")
    @PostMapping("/source/save")
    public Result<ContentSubscriptionSourceVO> saveSource(@Valid @RequestBody ContentSubscriptionSourceReq req) {
        return Result.OK(sourceService.saveSource(req));
    }

    /**
     * 查询订阅广场。
     */
    @Operation(summary = "查询订阅广场")
    @GetMapping("/plaza")
    public Result<ContentSubscriptionSourcePageVO> plaza(@RequestParam("userId") String userId,
                                                        @RequestParam(value = "category", required = false) String category,
                                                        @RequestParam(value = "keyword", required = false) String keyword,
                                                        @RequestParam(value = "sourceType", required = false) String sourceType,
                                                        @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
                                                        @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(sourceService.listPlaza(userId, category, keyword, sourceType, pageNo, pageSize));
    }

    /**
     * 查询订阅源详情。
     */
    @Operation(summary = "查询订阅源详情")
    @GetMapping("/source/detail")
    public Result<ContentSubscriptionSourceDetailVO> sourceDetail(@RequestParam("userId") String userId,
                                                                 @RequestParam("sourceType") String sourceType,
                                                                 @RequestParam("sourceId") String sourceId) {
        return Result.OK(sourceService.getSourceDetail(userId, sourceType, sourceId));
    }

    /**
     * 从订阅广场订阅内容源。
     */
    @Operation(summary = "从订阅广场订阅内容源")
    @PostMapping("/source/subscribe")
    public Result<ContentUserSubscriptionVO> subscribeSource(@RequestParam("userId") String userId,
                                                            @RequestParam("sourceType") String sourceType,
                                                            @RequestParam("sourceId") String sourceId) {
        return Result.OK(sourceService.subscribeFromPlaza(userId, sourceType, sourceId));
    }

    /**
     * 批量暂停订阅。
     */
    @Operation(summary = "批量暂停订阅")
    @PostMapping("/batch/pause")
    public Result<ContentSubscriptionBatchResultVO> batchPause(@RequestParam("userId") String userId,
                                                              @Valid @RequestBody ContentSubscriptionBatchReq req) {
        return Result.OK(subscriptionService.batchPause(userId, req.getSubscriptionIds()));
    }

    /**
     * 批量恢复订阅。
     */
    @Operation(summary = "批量恢复订阅")
    @PostMapping("/batch/resume")
    public Result<ContentSubscriptionBatchResultVO> batchResume(@RequestParam("userId") String userId,
                                                               @Valid @RequestBody ContentSubscriptionBatchReq req) {
        return Result.OK(subscriptionService.batchResume(userId, req.getSubscriptionIds()));
    }

    /**
     * 批量取消订阅。
     */
    @Operation(summary = "批量取消订阅")
    @PostMapping("/batch/cancel")
    public Result<ContentSubscriptionBatchResultVO> batchCancel(@RequestParam("userId") String userId,
                                                               @Valid @RequestBody ContentSubscriptionBatchReq req) {
        return Result.OK(subscriptionService.batchCancel(userId, req.getSubscriptionIds()));
    }

    /**
     * 保存订阅级通知偏好。
     */
    @Operation(summary = "保存订阅级通知偏好")
    @PostMapping("/notification/preference")
    public Result<ContentSubscriptionNotificationPreferenceVO> saveNotificationPreference(@RequestParam("userId") String userId,
                                                                                         @Valid @RequestBody ContentSubscriptionNotificationPreferenceReq req) {
        return Result.OK(notificationPreferenceService.savePreference(userId, req));
    }

    /**
     * 查询订阅级有效通知偏好。
     */
    @Operation(summary = "查询订阅级有效通知偏好")
    @GetMapping("/notification/preference")
    public Result<ContentSubscriptionNotificationPreferenceVO> getNotificationPreference(@RequestParam("userId") String userId,
                                                                                        @RequestParam("subscriptionId") String subscriptionId) {
        return Result.OK(notificationPreferenceService.getEffectivePreference(userId, subscriptionId));
    }

    /**
     * 计算订阅源更新通知决策。
     */
    @Operation(summary = "计算订阅源更新通知决策")
    @GetMapping("/notification/decision")
    public Result<ContentSubscriptionNotificationDecisionVO> notificationDecision(@RequestParam("userId") String userId,
                                                                                 @RequestParam("subscriptionId") String subscriptionId,
                                                                                 @RequestParam("updateBizId") String updateBizId) {
        return Result.OK(notificationPreferenceService.decideUpdateNotification(userId, subscriptionId, updateBizId));
    }
}
