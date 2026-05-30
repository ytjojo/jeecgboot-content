package org.jeecg.modules.content.channel.biz;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.GovernanceAction;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class ChannelGovernanceBizService {

    @Resource
    private ChannelMemberService memberService;
    @Resource
    private ChannelMuteService muteService;
    @Resource
    private ChannelBlacklistService blacklistService;
    @Resource
    private ChannelGovernanceLogService logService;

    @Transactional
    public void removeMember(String memberId, String operatorId, String reason) {
        ChannelMember member = memberService.getById(memberId);
        if (member == null) {
            throw new JeecgBootException("成员不存在");
        }
        if (member.getRole() == MemberRole.OWNER.getCode()) {
            throw new JeecgBootException("不能移除频道主");
        }
        ChannelMember operator = memberService.getByChannelAndUser(member.getChannelId(), operatorId);
        if (operator == null || operator.getRole() > member.getRole()) {
            throw new JeecgBootException("权限不足");
        }
        // Set 7-day cooling period
        member.setCoolingEndTime(Date.from(LocalDateTime.now().plusDays(7)
            .atZone(ZoneId.systemDefault()).toInstant()));
        memberService.updateById(member);
        memberService.removeMember(memberId);
        logService.log(GovernanceAction.REMOVE, member.getChannelId(), operatorId, member.getUserId(), reason, null);
    }

    @Transactional
    public void muteMember(String channelId, String userId, String operatorId, String reason, int days) {
        checkOperatorPermission(channelId, operatorId, userId);
        muteService.mute(channelId, userId, operatorId, reason, days);
        logService.log(GovernanceAction.MUTE, channelId, operatorId, userId, reason, null);
    }

    @Transactional
    public void unmuteMember(String channelId, String userId, String operatorId) {
        muteService.unmute(channelId, userId, operatorId);
        logService.log(GovernanceAction.UNMUTE, channelId, operatorId, userId, null, null);
    }

    @Transactional
    public void addToBlacklist(String channelId, String userId, String operatorId, String reason) {
        checkOperatorPermission(channelId, operatorId, userId);
        // If user is a member, remove them first
        ChannelMember member = memberService.getByChannelAndUser(channelId, userId);
        if (member != null) {
            memberService.removeMember(member.getId());
        }
        blacklistService.addToBlacklist(channelId, userId, operatorId, reason);
        logService.log(GovernanceAction.BLACKLIST_ADD, channelId, operatorId, userId, reason, null);
    }

    @Transactional
    public void removeFromBlacklist(String channelId, String userId, String operatorId) {
        blacklistService.removeFromBlacklist(channelId, userId, operatorId);
        logService.log(GovernanceAction.BLACKLIST_REMOVE, channelId, operatorId, userId, null, null);
    }

    private void checkOperatorPermission(String channelId, String operatorId, String targetUserId) {
        ChannelMember operator = memberService.getByChannelAndUser(channelId, operatorId);
        if (operator == null || operator.getRole() > MemberRole.ADMIN.getCode()) {
            throw new JeecgBootException("权限不足");
        }
        if (targetUserId.equals(operatorId)) {
            throw new JeecgBootException("不能对自己执行此操作");
        }
        ChannelMember target = memberService.getByChannelAndUser(channelId, targetUserId);
        if (target != null && target.getRole() == MemberRole.OWNER.getCode()) {
            throw new JeecgBootException("不能对频道主执行此操作");
        }
        if (target != null && operator.getRole() >= target.getRole()) {
            throw new JeecgBootException("权限不足");
        }
    }
}
