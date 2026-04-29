package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.vo.ContentUserProfileVO;
import org.springframework.web.bind.annotation.*;

/**
 * ReST endpoints for content user profile.
 */
@Tag(name = "内容社区用户资料")
@RestController
@RequestMapping("/content/user/profile")
public class ContentUserProfileController {

    @Resource
    private IContentUserProfileService profileService;

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
    public Result<String> updateProfile(@RequestParam("userId") String userId,
                                        @RequestBody ContentUserProfileUpdateReq req) {
        profileService.updateProfile(userId, req);
        return Result.OK("更新成功");
    }
}
