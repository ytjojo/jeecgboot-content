package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.channel.biz.ChannelMemberBizService;
import org.jeecg.modules.content.channel.entity.ChannelInvite;
import org.jeecg.modules.content.channel.service.ChannelInviteService;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "频道邀请管理")
@RestController
@RequestMapping("/api/v1/content/channel/invite")
public class ChannelInviteController {

    @Resource
    private ChannelInviteService inviteService;
    @Resource
    private ChannelMemberBizService memberBizService;
    @Resource
    private ChannelMemberService memberService;

    @Operation(summary = "创建邀请")
    @PostMapping("/create")
    public Result<ChannelInvite> createInvite(@RequestParam String channelId,
                                              @RequestParam Integer type,
                                              @RequestParam(required = false) Integer maxUses,
                                              @RequestParam(required = false) Integer expireDays) {
        String operatorId = SecureUtil.currentUser().getId();
        ChannelInvite invite = inviteService.createInvite(channelId, type, maxUses, expireDays, operatorId);
        return Result.OK(invite);
    }

    @Operation(summary = "查看邀请列表")
    @GetMapping("/list")
    public Result<List<ChannelInvite>> listInvites(@RequestParam String channelId) {
        return Result.OK(inviteService.listByChannel(channelId));
    }

    @Operation(summary = "撤销邀请")
    @PostMapping("/revoke")
    public Result<String> revokeInvite(@RequestParam String inviteId) {
        String operatorId = SecureUtil.currentUser().getId();
        inviteService.revokeInvite(inviteId, operatorId);
        return Result.OK("已撤销");
    }

    @Operation(summary = "使用邀请码加入")
    @PostMapping("/use")
    public Result<String> useInvite(@RequestParam String channelId,
                                    @RequestParam String code) {
        String userId = SecureUtil.currentUser().getId();
        memberBizService.joinByInvite(channelId, userId, code);
        return Result.OK("加入成功");
    }
}
