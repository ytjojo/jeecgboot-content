package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelGovernanceBizService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@Tag(name = "频道治理管理")
@RestController
@RequestMapping("/channel/governance")
public class ChannelGovernanceController {

    @Resource
    private ChannelGovernanceBizService governanceBizService;

    @Operation(summary = "移除成员")
    @PostMapping("/remove")
    public Result<String> removeMember(@RequestParam String memberId,
                                       @RequestParam(required = false) String reason) {
        String operatorId = "currentUserId";
        governanceBizService.removeMember(memberId, operatorId, reason);
        return Result.OK("已移除");
    }

    @Operation(summary = "禁言成员")
    @PostMapping("/mute")
    public Result<String> muteMember(@RequestParam String channelId,
                                     @RequestParam String userId,
                                     @RequestParam int days,
                                     @RequestParam(required = false) String reason) {
        String operatorId = "currentUserId";
        governanceBizService.muteMember(channelId, userId, operatorId, reason, days);
        return Result.OK("已禁言");
    }

    @Operation(summary = "解除禁言")
    @PostMapping("/unmute")
    public Result<String> unmuteMember(@RequestParam String channelId,
                                       @RequestParam String userId) {
        String operatorId = "currentUserId";
        governanceBizService.unmuteMember(channelId, userId, operatorId);
        return Result.OK("已解除禁言");
    }

    @Operation(summary = "加入黑名单")
    @PostMapping("/blacklist/add")
    public Result<String> addToBlacklist(@RequestParam String channelId,
                                         @RequestParam String userId,
                                         @RequestParam(required = false) String reason) {
        String operatorId = "currentUserId";
        governanceBizService.addToBlacklist(channelId, userId, operatorId, reason);
        return Result.OK("已加入黑名单");
    }

    @Operation(summary = "移出黑名单")
    @PostMapping("/blacklist/remove")
    public Result<String> removeFromBlacklist(@RequestParam String channelId,
                                               @RequestParam String userId) {
        String operatorId = "currentUserId";
        governanceBizService.removeFromBlacklist(channelId, userId, operatorId);
        return Result.OK("已移出黑名单");
    }
}
