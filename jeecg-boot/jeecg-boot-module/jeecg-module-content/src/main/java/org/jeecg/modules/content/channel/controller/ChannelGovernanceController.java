package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelGovernanceBizService;
import org.jeecg.modules.content.channel.entity.ChannelBlacklist;
import org.jeecg.modules.content.channel.entity.ChannelGovernanceLog;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.service.ChannelBlacklistService;
import org.jeecg.modules.content.channel.service.ChannelGovernanceLogService;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.jeecg.modules.content.channel.util.ChannelSecurityUtil;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "频道治理管理")
@RestController
@RequestMapping("/api/v1/content/channel/governance")
public class ChannelGovernanceController {

    @Resource
    private ChannelGovernanceBizService governanceBizService;
    @Resource
    private ChannelBlacklistService blacklistService;
    @Resource
    private ChannelGovernanceLogService governanceLogService;
    @Resource
    private ChannelMemberService memberService;

    @Operation(summary = "移除成员")
    @PostMapping("/remove")
    public Result<String> removeMember(@RequestParam String memberId,
                                       @RequestParam(required = false) String reason) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        ChannelMember targetMember = memberService.getById(memberId);
        if (targetMember == null) {
            return Result.error("成员不存在");
        }
        ChannelSecurityUtil.checkChannelAdminPermission(memberService, targetMember.getChannelId(), operatorId);
        governanceBizService.removeMember(memberId, operatorId, reason);
        return Result.OK("已移除");
    }

    @Operation(summary = "禁言成员")
    @PostMapping("/mute")
    public Result<String> muteMember(@RequestParam String channelId,
                                     @RequestParam String userId,
                                     @RequestParam int days,
                                     @RequestParam(required = false) String reason) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        ChannelSecurityUtil.checkChannelAdminPermission(memberService, channelId, operatorId);
        governanceBizService.muteMember(channelId, userId, operatorId, reason, days);
        return Result.OK("已禁言");
    }

    @Operation(summary = "解除禁言")
    @PostMapping("/unmute")
    public Result<String> unmuteMember(@RequestParam String channelId,
                                       @RequestParam String userId) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        ChannelSecurityUtil.checkChannelAdminPermission(memberService, channelId, operatorId);
        governanceBizService.unmuteMember(channelId, userId, operatorId);
        return Result.OK("已解除禁言");
    }

    @Operation(summary = "加入黑名单")
    @PostMapping("/blacklist/add")
    public Result<String> addToBlacklist(@RequestParam String channelId,
                                         @RequestParam String userId,
                                         @RequestParam(required = false) String reason) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        ChannelSecurityUtil.checkChannelAdminPermission(memberService, channelId, operatorId);
        governanceBizService.addToBlacklist(channelId, userId, operatorId, reason);
        return Result.OK("已加入黑名单");
    }

    @Operation(summary = "移出黑名单")
    @PostMapping("/blacklist/remove")
    public Result<String> removeFromBlacklist(@RequestParam String channelId,
                                               @RequestParam String userId) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        ChannelSecurityUtil.checkChannelAdminPermission(memberService, channelId, operatorId);
        governanceBizService.removeFromBlacklist(channelId, userId, operatorId);
        return Result.OK("已移出黑名单");
    }

    @Operation(summary = "黑名单列表")
    @GetMapping("/blacklist/list")
    public Result<List<ChannelBlacklist>> listBlacklist(@RequestParam String channelId) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        ChannelSecurityUtil.checkChannelAdminPermission(memberService, channelId, operatorId);
        return Result.OK(blacklistService.listByChannel(channelId));
    }

    @Operation(summary = "治理日志列表")
    @GetMapping("/log")
    public Result<IPage<ChannelGovernanceLog>> listGovernanceLogs(
            @RequestParam String channelId,
            @RequestParam(required = false) Integer action,
            @RequestParam(defaultValue = "1") int pageNum,
            @Max(100) @RequestParam(defaultValue = "20") int pageSize) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        ChannelSecurityUtil.checkChannelAdminPermission(memberService, channelId, operatorId);
        return Result.OK(governanceLogService.listByChannel(channelId, action, pageNum, pageSize));
    }
}
