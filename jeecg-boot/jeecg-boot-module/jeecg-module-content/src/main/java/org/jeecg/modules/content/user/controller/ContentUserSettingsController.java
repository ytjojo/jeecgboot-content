package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq;
import org.jeecg.modules.content.user.req.settings.ContentUserNotificationUpdateReq;
import org.jeecg.modules.content.user.service.IContentUserNotificationSettingService;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.service.IContentUserVisibilityPolicyService;
import org.jeecg.modules.content.user.vo.ContentUserNotificationSettingVO;
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
     * 检查当前查看者是否可见目标内容。
     */
    @Operation(summary = "检查是否允许查看内容")
    @GetMapping("/visibility/content")
    public Result<Boolean> canViewContent(@RequestParam("ownerUserId") String ownerUserId,
                                          @RequestParam("viewerUserId") String viewerUserId) {
        return Result.OK(visibilityPolicyService.canViewContent(ownerUserId, viewerUserId));
    }
}
