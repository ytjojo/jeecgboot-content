package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.entity.ContentUserPrivacySetting;
import org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq;
import org.jeecg.modules.content.user.req.settings.ContentFeedSettingUpdateReq;
import org.jeecg.modules.content.user.req.settings.ContentNotificationDndRuleReq;
import org.jeecg.modules.content.user.req.settings.ContentUserNotificationUpdateReq;
import org.jeecg.modules.content.user.req.settings.ContentUserSecurityUpdateReq;
import org.jeecg.modules.content.user.service.IContentUserFeedSettingService;
import org.jeecg.modules.content.user.service.IContentUserNotificationSettingService;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.service.IContentUserSecuritySettingService;
import org.jeecg.modules.content.user.service.IContentUserVisibilityPolicyService;
import org.jeecg.modules.content.user.vo.ContentNotificationDndRuleVO;
import org.jeecg.modules.content.user.vo.ContentUserFeedSettingVO;
import org.jeecg.modules.content.user.vo.ContentUserNotificationSettingVO;
import org.jeecg.modules.content.user.vo.ContentUserSecuritySettingVO;
import org.springframework.web.bind.annotation.*;

/**
 * ReST endpoints for content user settings.
 */
@Tag(name = "内容社区用户设置")
@RestController
@RequestMapping("/content/user/settings")
public class ContentUserSettingsController {

    @Resource
    private IContentUserProfileService profileService;

    @Resource
    private IContentUserVisibilityPolicyService visibilityPolicyService;

    @Resource
    private IContentUserNotificationSettingService notificationSettingService;

    @Resource
    private IContentUserFeedSettingService feedSettingService;

    @Resource
    private IContentUserSecuritySettingService securitySettingService;

    /**
     * 查询用户隐私设置。
     */
    @Operation(summary = "查询隐私设置")
    @GetMapping("/privacy")
    public Result<ContentUserPrivacySetting> getPrivacy(@RequestParam("userId") String userId) {
        return Result.OK(profileService.getPrivacySetting(userId));
    }

    /**
     * 更新用户隐私、可见性与发现设置。
     */
    @Operation(summary = "更新隐私设置")
    @PostMapping("/privacy/update")
    public Result<String> updatePrivacy(@RequestParam("userId") String userId,
                                        @Valid @RequestBody ContentUserPrivacyUpdateReq req) {
        profileService.updatePrivacy(userId, req);
        return Result.OK("更新成功");
    }

    /**
     * 查询用户通知设置。
     */
    @Operation(summary = "查询通知设置")
    @GetMapping("/notification")
    public Result<ContentUserNotificationSettingVO> getNotification(@RequestParam("userId") String userId) {
        return Result.OK(notificationSettingService.getSetting(userId));
    }

    /**
     * 更新用户通知开关、通知渠道和免打扰规则。
     */
    @Operation(summary = "更新通知设置")
    @PostMapping("/notification/update")
    public Result<ContentUserNotificationSettingVO> updateNotification(@RequestParam("userId") String userId,
                                                                       @Valid @RequestBody ContentUserNotificationUpdateReq req) {
        return Result.OK(notificationSettingService.updateSetting(userId, req));
    }

    /**
     * 查询关注流动态类型设置。
     */
    @Operation(summary = "查询关注流动态类型设置")
    @GetMapping("/feed")
    public Result<ContentUserFeedSettingVO> getFeedSetting(@RequestParam("userId") String userId) {
        return Result.OK(feedSettingService.getSetting(userId));
    }

    /**
     * 更新关注流动态类型设置。
     */
    @Operation(summary = "更新关注流动态类型设置")
    @PostMapping("/feed/update")
    public Result<ContentUserFeedSettingVO> updateFeedSetting(@RequestParam("userId") String userId,
                                                              @Valid @RequestBody ContentFeedSettingUpdateReq req) {
        return Result.OK(feedSettingService.updateSetting(userId, req));
    }

    /**
     * 检查当前查看者是否可见目标内容。
     */
    @Operation(summary = "检查是否允许查看内容")
    @GetMapping("/visibility/content")
    public Result<Boolean> canViewContent(@RequestParam("ownerUserId") String ownerUserId,
                                          @RequestParam("viewerUserId") String viewerUserId) {
        return Result.OK(visibilityPolicyService.canViewContent(ownerUserId, viewerUserId));
    }

    /**
     * 单独更新免打扰规则。
     */
    @Operation(summary = "更新免打扰规则")
    @PostMapping("/notification/dnd/update")
    public Result<ContentNotificationDndRuleVO> updateDndRule(@RequestParam("userId") String userId,
                                                              @Valid @RequestBody ContentNotificationDndRuleReq req) {
        return Result.OK(notificationSettingService.updateDndRule(userId, req));
    }

    /**
     * 查询用户账号安全设置。
     */
    @Operation(summary = "查询账号安全设置")
    @GetMapping("/security")
    public Result<ContentUserSecuritySettingVO> getSecuritySetting(@RequestParam("userId") String userId) {
        return Result.OK(securitySettingService.getSecuritySetting(userId));
    }

    /**
     * 更新用户账号安全设置。
     */
    @Operation(summary = "更新账号安全设置")
    @PostMapping("/security/update")
    public Result<ContentUserSecuritySettingVO> updateSecuritySetting(@RequestParam("userId") String userId,
                                                                      @Valid @RequestBody ContentUserSecurityUpdateReq req) {
        return Result.OK(securitySettingService.updateSecuritySetting(userId, req));
    }
}
