package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.relation.ContentFollowReq;
import org.jeecg.modules.content.user.service.IContentUserRelationService;
import org.jeecg.modules.content.user.vo.ContentUserRelationVO;
import org.springframework.web.bind.annotation.*;

/**
 * ReST endpoints for content user relation.
 */
@Tag(name = "内容社区用户关系")
@RestController
@RequestMapping("/content/user/relation")
public class ContentUserRelationController {

    @Resource
    private IContentUserRelationService relationService;

    /**
     * Creates or refreshes a follow relationship to the target user.
     */
    @Operation(summary = "关注用户")
    @PostMapping("/follow")
    public Result<String> follow(@RequestParam("userId") String userId, @Valid @RequestBody ContentFollowReq req) {
        relationService.follow(userId, req.getTargetUserId(), req.getRelationGroupId());
        return Result.OK("关注成功");
    }

    /**
     * Marks the target user as special follow.
     */
    @Operation(summary = "特别关注用户")
    @PostMapping("/special-follow")
    public Result<String> specialFollow(@RequestParam("userId") String userId,
                                        @Valid @RequestBody ContentFollowReq req) {
        relationService.specialFollow(userId, req.getTargetUserId(), req.getRelationGroupId());
        return Result.OK("特别关注成功");
    }

    /**
     * Cancels the follow relationship to the target user.
     */
    @Operation(summary = "取消关注")
    @PostMapping("/unfollow")
    public Result<String> unfollow(@RequestParam("userId") String userId,
                                   @RequestParam("targetUserId") String targetUserId) {
        relationService.unfollow(userId, targetUserId);
        return Result.OK("取消关注成功");
    }

    /**
     * Blacklists the target user and cuts off related interactions.
     */
    @Operation(summary = "拉黑用户")
    @PostMapping("/blacklist")
    public Result<String> blacklist(@RequestParam("userId") String userId,
                                    @RequestParam("targetUserId") String targetUserId) {
        relationService.blacklist(userId, targetUserId);
        return Result.OK("拉黑成功");
    }

    /**
     * Removes the blacklist for the target user.
     */
    @Operation(summary = "解除拉黑")
    @PostMapping("/blacklist/cancel")
    public Result<String> unblacklist(@RequestParam("userId") String userId,
                                      @RequestParam("targetUserId") String targetUserId) {
        relationService.unblacklist(userId, targetUserId);
        return Result.OK("解除拉黑成功");
    }

    /**
     * Mutes the target user for one-way noise reduction.
     */
    @Operation(summary = "屏蔽用户")
    @PostMapping("/mute")
    public Result<String> mute(@RequestParam("userId") String userId,
                               @RequestParam("targetUserId") String targetUserId) {
        relationService.mute(userId, targetUserId);
        return Result.OK("屏蔽成功");
    }

    /**
     * Removes the mute flag for the target user.
     */
    @Operation(summary = "解除屏蔽")
    @PostMapping("/mute/cancel")
    public Result<String> unmute(@RequestParam("userId") String userId,
                                 @RequestParam("targetUserId") String targetUserId) {
        relationService.unmute(userId, targetUserId);
        return Result.OK("解除屏蔽成功");
    }

    /**
     * Removes the special follow flag for the target user.
     */
    @Operation(summary = "取消特别关注")
    @PostMapping("/special-follow/cancel")
    public Result<String> cancelSpecialFollow(@RequestParam("userId") String userId,
                                              @RequestParam("targetUserId") String targetUserId) {
        relationService.cancelSpecialFollow(userId, targetUserId);
        return Result.OK("取消特别关注成功");
    }

    /**
     * Gets the relation details between the current user and the target user.
     */
    @Operation(summary = "查询关系")
    @GetMapping("/detail")
    public Result<ContentUserRelationVO> detail(@RequestParam("userId") String userId,
                                                @RequestParam("targetUserId") String targetUserId) {
        return Result.OK(relationService.getRelation(userId, targetUserId));
    }
}
