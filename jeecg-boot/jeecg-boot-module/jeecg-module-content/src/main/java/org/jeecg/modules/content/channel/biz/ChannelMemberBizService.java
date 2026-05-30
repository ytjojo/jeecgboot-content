package org.jeecg.modules.content.channel.biz;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.JoinMethod;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

@Service
public class ChannelMemberBizService {

    @Resource
    private ChannelService channelService;
    @Resource
    private ChannelMemberService memberService;
    @Resource
    private ChannelBlacklistService blacklistService;
    @Resource
    private ChannelInviteService inviteService;
    @Resource
    private ChannelJoinApplicationService applicationService;

    @Transactional
    public void joinByFree(String channelId, String userId) {
        Channel channel = channelService.getById(channelId);
        if (channel == null) {
            throw new JeecgBootException("频道不存在");
        }
        if (channel.getJoinMethod() != null && channel.getJoinMethod() != JoinMethod.FREE.getCode()) {
            throw new JeecgBootException("该频道不是自由加入模式");
        }
        checkBlacklist(channelId, userId);
        memberService.addMember(channelId, userId, MemberRole.MEMBER);
    }

    @Transactional
    public void joinByReview(String channelId, String userId, String reason) {
        Channel channel = channelService.getById(channelId);
        if (channel == null) {
            throw new JeecgBootException("频道不存在");
        }
        checkBlacklist(channelId, userId);
        applicationService.apply(channelId, userId, reason);
    }

    @Transactional
    public void joinByInvite(String channelId, String userId, String inviteCode) {
        Channel channel = channelService.getById(channelId);
        if (channel == null) {
            throw new JeecgBootException("频道不存在");
        }
        checkBlacklist(channelId, userId);
        inviteService.validateInvite(inviteCode);
        inviteService.useInvite(inviteCode);
        memberService.addMember(channelId, userId, MemberRole.MEMBER);
    }

    @Transactional
    public void approveAndAddMember(String applicationId, String reviewerId, String reviewReason) {
        applicationService.approve(applicationId, reviewerId, reviewReason);
        var app = applicationService.getById(applicationId);
        if (app != null) {
            memberService.addMember(app.getChannelId(), app.getUserId(), MemberRole.MEMBER);
        }
    }

    private void checkBlacklist(String channelId, String userId) {
        if (blacklistService.isBlacklisted(channelId, userId)) {
            throw new JeecgBootException("您无法加入此频道");
        }
    }
}
