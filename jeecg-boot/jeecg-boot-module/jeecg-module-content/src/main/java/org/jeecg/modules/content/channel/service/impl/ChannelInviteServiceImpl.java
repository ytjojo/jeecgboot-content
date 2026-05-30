package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelInvite;
import org.jeecg.modules.content.channel.enums.InviteStatus;
import org.jeecg.modules.content.channel.mapper.ChannelInviteMapper;
import org.jeecg.modules.content.channel.service.ChannelInviteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ChannelInviteServiceImpl extends ServiceImpl<ChannelInviteMapper, ChannelInvite>
    implements ChannelInviteService {

    @Override
    public ChannelInvite createInvite(String channelId, Integer type, Integer maxUses, Integer expireDays, String creatorId) {
        ChannelInvite invite = new ChannelInvite();
        invite.setChannelId(channelId);
        invite.setCode(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        invite.setType(type);
        invite.setMaxUses(maxUses);
        invite.setUsedCount(0);
        if (expireDays != null && expireDays > 0) {
            invite.setExpireTime(Date.from(LocalDateTime.now().plusDays(expireDays)
                .atZone(ZoneId.systemDefault()).toInstant()));
        }
        invite.setStatus(InviteStatus.ACTIVE.getCode());
        invite.setCreatorId(creatorId);
        baseMapper.insert(invite);
        return invite;
    }

    @Override
    public boolean validateInvite(String code) {
        ChannelInvite invite = getOne(new LambdaQueryWrapper<ChannelInvite>()
            .eq(ChannelInvite::getCode, code));
        if (invite == null) {
            throw new JeecgBootException("邀请码不存在");
        }
        if (invite.getStatus() != InviteStatus.ACTIVE.getCode()) {
            throw new JeecgBootException("邀请码已失效");
        }
        if (invite.getExpireTime() != null && invite.getExpireTime().before(new Date())) {
            throw new JeecgBootException("邀请码已过期");
        }
        if (invite.getMaxUses() != null && invite.getUsedCount() >= invite.getMaxUses()) {
            throw new JeecgBootException("邀请码已用完");
        }
        return true;
    }

    @Override
    @Transactional
    public void useInvite(String code) {
        ChannelInvite invite = getOne(new LambdaQueryWrapper<ChannelInvite>()
            .eq(ChannelInvite::getCode, code));
        if (invite == null) {
            throw new JeecgBootException("邀请码不存在");
        }
        validateInvite(code);
        int newCount = invite.getUsedCount() + 1;
        boolean usedUp = invite.getMaxUses() != null && newCount >= invite.getMaxUses();
        baseMapper.update(null, new LambdaUpdateWrapper<ChannelInvite>()
            .eq(ChannelInvite::getCode, code)
            .set(ChannelInvite::getUsedCount, newCount)
            .set(usedUp, ChannelInvite::getStatus, InviteStatus.USED_UP.getCode()));
    }

    @Override
    public void revokeInvite(String inviteId, String operatorId) {
        ChannelInvite invite = getById(inviteId);
        if (invite == null) {
            throw new JeecgBootException("邀请不存在");
        }
        invite.setStatus(InviteStatus.REVOKED.getCode());
        updateById(invite);
    }

    @Override
    public List<ChannelInvite> listByChannel(String channelId) {
        return list(new LambdaQueryWrapper<ChannelInvite>()
            .eq(ChannelInvite::getChannelId, channelId)
            .orderByDesc(ChannelInvite::getCreateTime));
    }
}
