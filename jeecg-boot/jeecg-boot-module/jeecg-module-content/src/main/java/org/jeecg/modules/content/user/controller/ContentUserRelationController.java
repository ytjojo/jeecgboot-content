package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.relation.ContentFollowReq;
import org.jeecg.modules.content.user.req.relation.ContentRelationBatchReq;
import org.jeecg.modules.content.user.req.relation.ContentRelationGroupMoveReq;
import org.jeecg.modules.content.user.req.relation.ContentRelationGroupRemoveReq;
import org.jeecg.modules.content.user.req.relation.ContentRelationGroupReq;
import org.jeecg.modules.content.user.service.IContentUserFollowRecommendationService;
import org.jeecg.modules.content.user.service.IContentUserRelationService;
import org.jeecg.modules.content.user.vo.ContentBlockMuteHelpVO;
import org.jeecg.modules.content.user.vo.ContentRelationBatchResultVO;
import org.jeecg.modules.content.user.vo.ContentRelationGroupVO;
import org.jeecg.modules.content.user.vo.ContentRelationUserPageVO;
import org.jeecg.modules.content.user.vo.ContentFollowFeedPageVO;
import org.jeecg.modules.content.user.vo.ContentFollowRecommendationPageVO;
import org.jeecg.modules.content.user.vo.ContentUserBlacklistPageVO;
import org.jeecg.modules.content.user.vo.ContentUserRelationVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ReST endpoints for content user relation.
 */
@Tag(name = "内容社区用户关系")
@RestController
@RequestMapping("/content/user/relation")
public class ContentUserRelationController {

    @Resource
    private IContentUserRelationService relationService;

    @Resource
    private IContentUserFollowRecommendationService recommendationService;

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

    /**
     * Lists the current user's relation groups.
     */
    @Operation(summary = "查询关注分组")
    @GetMapping("/groups")
    public Result<List<ContentRelationGroupVO>> groups(@RequestParam("userId") String userId) {
        return Result.OK(relationService.listGroups(userId));
    }

    /**
     * Creates a custom relation group.
     */
    @Operation(summary = "创建关注分组")
    @PostMapping("/group/create")
    public Result<ContentRelationGroupVO> createGroup(@RequestParam("userId") String userId,
                                                      @Valid @RequestBody ContentRelationGroupReq req) {
        return Result.OK(relationService.createGroup(userId, req));
    }

    /**
     * Renames and reorders a custom relation group.
     */
    @Operation(summary = "重命名关注分组")
    @PostMapping("/group/rename")
    public Result<ContentRelationGroupVO> renameGroup(@RequestParam("userId") String userId,
                                                      @RequestParam("groupId") String groupId,
                                                      @Valid @RequestBody ContentRelationGroupReq req) {
        return Result.OK(relationService.renameGroup(userId, groupId, req));
    }

    /**
     * Deletes a custom relation group.
     */
    @Operation(summary = "删除关注分组")
    @PostMapping("/group/delete")
    public Result<String> deleteGroup(@RequestParam("userId") String userId,
                                      @RequestParam("groupId") String groupId) {
        relationService.deleteGroup(userId, groupId);
        return Result.OK("删除分组成功");
    }

    /**
     * Moves followed users into a relation group.
     */
    @Operation(summary = "移动关注对象到分组")
    @PostMapping("/group/move")
    public Result<ContentRelationBatchResultVO> moveToGroup(@RequestParam("userId") String userId,
                                                            @Valid @RequestBody ContentRelationGroupMoveReq req) {
        return Result.OK(relationService.moveTargetsToGroup(userId, req.getTargetUserIds(), req.getRelationGroupId()));
    }

    /**
     * Removes followed users from custom groups and falls back to the default group.
     */
    @Operation(summary = "移出关注分组")
    @PostMapping("/group/remove")
    public Result<ContentRelationBatchResultVO> removeFromGroup(@RequestParam("userId") String userId,
                                                                @Valid @RequestBody ContentRelationGroupRemoveReq req) {
        return Result.OK(relationService.removeTargetsFromGroup(userId, req.getTargetUserIds()));
    }

    /**
     * 批量取消关注。
     */
    @Operation(summary = "批量取消关注")
    @PostMapping("/batch/unfollow")
    public Result<ContentRelationBatchResultVO> batchUnfollow(@RequestParam("userId") String userId,
                                                              @Valid @RequestBody ContentRelationBatchReq req) {
        return Result.OK(relationService.batchUnfollow(userId, req.getTargetUserIds()));
    }

    /**
     * 批量取消特别关注。
     */
    @Operation(summary = "批量取消特别关注")
    @PostMapping("/batch/special-follow/cancel")
    public Result<ContentRelationBatchResultVO> batchCancelSpecialFollow(@RequestParam("userId") String userId,
                                                                         @Valid @RequestBody ContentRelationBatchReq req) {
        return Result.OK(relationService.batchCancelSpecialFollow(userId, req.getTargetUserIds()));
    }

    /**
     * 分页查询当前用户关注列表。
     */
    @Operation(summary = "分页查询关注列表")
    @GetMapping("/follow-list")
    public Result<ContentRelationUserPageVO> followList(@RequestParam("userId") String userId,
                                                        @RequestParam(value = "relationGroupId", required = false) String relationGroupId,
                                                        @RequestParam(value = "keyword", required = false) String keyword,
                                                        @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
                                                        @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(relationService.listFollowedUsers(userId, relationGroupId, keyword, pageNo, pageSize));
    }

    /**
     * 分页查询当前用户特别关注列表。
     */
    @Operation(summary = "分页查询特别关注列表")
    @GetMapping("/special-follow-list")
    public Result<ContentRelationUserPageVO> specialFollowList(@RequestParam("userId") String userId,
                                                               @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
                                                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(relationService.listSpecialFollowedUsers(userId, pageNo, pageSize));
    }

    /**
     * 分页查询当前用户黑名单。
     */
    @Operation(summary = "分页查询黑名单")
    @GetMapping("/blacklist")
    public Result<ContentUserBlacklistPageVO> blacklist(@RequestParam("userId") String userId,
                                                        @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
                                                        @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(relationService.listBlacklist(userId, pageNo, pageSize));
    }

    /**
     * 分页查询关注流动态。
     */
    @Operation(summary = "分页查询关注流")
    @GetMapping("/feed")
    public Result<ContentFollowFeedPageVO> followFeed(@RequestParam("userId") String userId,
                                                      @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
                                                      @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(relationService.listFollowFeed(userId, pageNo, pageSize));
    }

    /**
     * 分页查询互关好友列表。
     */
    @Operation(summary = "分页查询互关好友列表")
    @GetMapping("/mutual-follow-list")
    public Result<ContentRelationUserPageVO> mutualFollowList(
            @RequestParam("userId") String userId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(relationService.getMutualFollowList(userId, pageNo, pageSize));
    }

    /**
     * 返回拉黑、屏蔽、解除拉黑的确认文案和帮助说明。
     */
    @Operation(summary = "获取拉黑/屏蔽帮助说明")
    @GetMapping("/block-mute/help")
    public Result<ContentBlockMuteHelpVO> blockMuteHelp() {
        return Result.OK(relationService.getBlockMuteHelp());
    }

    /**
     * 分页查询互关好友列表。
     */
    @Operation(summary = "分页查询互关好友列表")
    @GetMapping("/mutual-follow-list")
    public Result<ContentRelationUserPageVO> mutualFollowList(@RequestParam("userId") String userId,
                                                               @RequestParam(value = "keyword", required = false) String keyword,
                                                               @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
                                                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(relationService.getMutualFollowList(userId, keyword, pageNo, pageSize));
    }

    /**
     * 分页查询关注推荐。
     */
    @Operation(summary = "分页查询关注推荐")
    @GetMapping("/recommendations")
    public Result<ContentFollowRecommendationPageVO> recommendations(@RequestParam("userId") String userId,
                                                                     @RequestParam(value = "interestTag", required = false) String interestTag,
                                                                     @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
                                                                     @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(recommendationService.listRecommendations(userId, interestTag, pageNo, pageSize));
    }
}
