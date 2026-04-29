package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.service.IContentUserVisibilityPolicyService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "内容社区用户设置")
@RestController
@RequestMapping("/content/user/settings")
public class ContentUserSettingsController {

    @Resource
    private IContentUserProfileService profileService;

    @Resource
    private IContentUserVisibilityPolicyService visibilityPolicyService;

    @Operation(summary = "更新隐私设置")
    @PostMapping("/privacy/update")
    public Result<String> updatePrivacy(@RequestParam("userId") String userId,
                                        @RequestBody ContentUserPrivacyUpdateReq req) {
        profileService.updatePrivacy(userId, req);
        return Result.OK("更新成功");
    }

    @Operation(summary = "检查是否允许查看内容")
    @GetMapping("/visibility/content")
    public Result<Boolean> canViewContent(@RequestParam("ownerUserId") String ownerUserId,
                                          @RequestParam("viewerUserId") String viewerUserId) {
        return Result.OK(visibilityPolicyService.canViewContent(ownerUserId, viewerUserId));
    }
}
