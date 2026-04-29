package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
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
    public Result<String> follow(@RequestParam("userId") String userId, @RequestBody ContentFollowReq req) {
        relationService.follow(userId, req.getTargetUserId(), req.getRelationGroupId());
        return Result.OK("关注成功");
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
     * Gets the relation details between the current user and the target user.
     */
    @Operation(summary = "查询关系")
    @GetMapping("/detail")
    public Result<ContentUserRelationVO> detail(@RequestParam("userId") String userId,
                                                @RequestParam("targetUserId") String targetUserId) {
        return Result.OK(relationService.getRelation(userId, targetUserId));
    }
}
