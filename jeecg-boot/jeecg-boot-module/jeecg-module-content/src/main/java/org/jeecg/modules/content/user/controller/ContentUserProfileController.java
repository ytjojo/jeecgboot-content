package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.profile.ContentUserHomepageUpdateReq;
import org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq;
import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;
import org.jeecg.modules.content.user.req.profile.ContentUserReviewHandleReq;
import org.jeecg.modules.content.user.service.IContentUserHomepageService;
import org.jeecg.modules.content.user.service.IContentUserProfileHistoryService;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.service.IContentUserVerificationBadgeService;
import org.jeecg.modules.content.user.vo.ContentUserHomepageModuleVO;
import org.jeecg.modules.content.user.vo.ContentUserProfileHistoryVO;
import org.jeecg.modules.content.user.vo.ContentUserProfileVO;
import org.jeecg.modules.content.user.vo.ContentUserVerificationBadgeVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ReST endpoints for content user profile.
 */
@Tag(name = "内容社区用户资料")
@RestController
@RequestMapping("/api/v1/content/user/profile")
public class ContentUserProfileController {

    @Resource
    private IContentUserProfileService profileService;

    @Resource
    private IContentUserHomepageService homepageService;

    @Resource
    private IContentUserVerificationBadgeService verificationBadgeService;

    @Resource
    private IContentUserProfileHistoryService profileHistoryService;

    /**
     * Returns the user profile as seen by the current viewer.
     */
    @Operation(summary = "获取用户资料")
    @GetMapping("/detail")
    public Result<ContentUserProfileVO> getProfile(@RequestParam("ownerUserId") String ownerUserId,
                                                   @RequestParam(value = "viewerUserId", required = false) String viewerUserId) {
        return Result.OK(profileService.getProfile(ownerUserId, viewerUserId));
    }

    /**
     * Updates user profile fields and homepage personalization settings.
     */
    @Operation(summary = "更新用户资料")
    @PostMapping("/update")
    public Result<ContentUserProfileVO> updateProfile(@RequestParam("userId") String userId,
                                                       @Valid @RequestBody ContentUserProfileUpdateReq req) {
        return Result.OK(profileService.updateProfile(userId, req));
    }

    /**
     * 处理资料审核结果。
     */
    @Operation(summary = "处理资料审核")
    @PostMapping("/review/handle")
    public Result<String> handleReview(@Valid @RequestBody ContentUserReviewHandleReq req) {
        profileService.handleProfileReview(req);
        return Result.OK("处理成功");
    }

    /**
     * 更新资料字段隐私配置。
     */
    @Operation(summary = "更新资料隐私配置")
    @PostMapping("/privacy/update")
    public Result<String> updatePrivacy(@RequestParam("userId") String userId,
                                        @Valid @RequestBody ContentUserPrivacyUpdateReq req) {
        profileService.updatePrivacy(userId, req);
        return Result.OK("更新成功");
    }

    /**
     * 更新主页个性化配置。
     */
    @Operation(summary = "更新主页个性化")
    @PostMapping("/homepage/update")
    public Result<ContentUserProfileVO> updateHomepage(@RequestParam("userId") String userId,
                                                        @Valid @RequestBody ContentUserHomepageUpdateReq req) {
        return Result.OK(homepageService.updateHomepage(userId, req));
    }

    /**
     * 恢复主页默认配置。
     */
    @Operation(summary = "恢复主页默认配置")
    @PostMapping("/homepage/defaults/restore")
    public Result<ContentUserProfileVO> restoreHomepageDefaults(@RequestParam("userId") String userId) {
        return Result.OK(homepageService.restoreDefaults(userId));
    }

    /**
     * 查询主页模块配置。
     */
    @Operation(summary = "查询主页模块配置")
    @GetMapping("/homepage/modules")
    public Result<List<ContentUserHomepageModuleVO>> listHomepageModules(@RequestParam("userId") String userId) {
        return Result.OK(homepageService.listModules(userId));
    }

    /**
     * 查询认证标识列表。
     */
    @Operation(summary = "查询认证标识列表")
    @GetMapping("/badge/list")
    public Result<List<ContentUserVerificationBadgeVO>> listBadges(@RequestParam("userId") String userId) {
        return Result.OK(verificationBadgeService.listVisibleBadges(userId));
    }

    /**
     * 查询认证标识详情。
     */
    @Operation(summary = "查询认证标识详情")
    @GetMapping("/badge/detail")
    public Result<ContentUserVerificationBadgeVO> badgeDetail(@RequestParam("badgeId") String badgeId) {
        return Result.OK(verificationBadgeService.getBadgeDetail(badgeId));
    }

    /**
     * 查询昵称或头像历史。
     */
    @Operation(summary = "查询资料历史")
    @GetMapping("/history/list")
    public Result<List<ContentUserProfileHistoryVO>> listHistory(@RequestParam("userId") String userId,
                                                                 @RequestParam("historyType") String historyType) {
        return Result.OK(profileHistoryService.listHistory(userId, historyType));
    }

    /**
     * 从历史记录恢复昵称或头像。
     */
    @Operation(summary = "恢复资料历史")
    @PostMapping("/history/restore")
    public Result<ContentUserProfileVO> restoreHistory(@RequestParam("userId") String userId,
                                                        @RequestParam("historyId") String historyId) {
        return Result.OK(profileHistoryService.restoreHistory(userId, historyId));
    }
}
