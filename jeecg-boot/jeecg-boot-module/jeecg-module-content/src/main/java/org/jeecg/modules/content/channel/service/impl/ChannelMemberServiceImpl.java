package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.mapper.ChannelMemberMapper;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ChannelMemberServiceImpl extends ServiceImpl<ChannelMemberMapper, ChannelMember>
    implements ChannelMemberService {

    @Override
    public void addMember(String channelId, String userId, MemberRole role) {
        ChannelMember existing = getByChannelAndUser(channelId, userId);
        if (existing != null) {
            if (existing.getCoolingEndTime() != null && existing.getCoolingEndTime().after(new Date())) {
                throw new JeecgBootException("冷却期内无法加入");
            }
            throw new JeecgBootException("已是频道成员");
        }
        ChannelMember member = new ChannelMember();
        member.setChannelId(channelId);
        member.setUserId(userId);
        member.setRole(role.getCode());
        member.setJoinTime(new Date());
        baseMapper.insert(member);
    }

    @Override
    public void removeMember(String memberId) {
        ChannelMember member = getById(memberId);
        if (member == null) {
            throw new JeecgBootException("成员不存在");
        }
        removeById(memberId);
    }

    @Override
    public void assignRole(String memberId, MemberRole role, String operatorId) {
        ChannelMember member = getById(memberId);
        if (member == null) {
            throw new JeecgBootException("成员不存在");
        }
        member.setRole(role.getCode());
        updateById(member);
    }

    @Override
    public boolean isMember(String channelId, String userId) {
        return getByChannelAndUser(channelId, userId) != null;
    }

    @Override
    public ChannelMember getByChannelAndUser(String channelId, String userId) {
        return getOne(new LambdaQueryWrapper<ChannelMember>()
            .eq(ChannelMember::getChannelId, channelId)
            .eq(ChannelMember::getUserId, userId));
    }

    @Override
    public List<ChannelMember> listByChannel(String channelId) {
        return list(new LambdaQueryWrapper<ChannelMember>()
            .eq(ChannelMember::getChannelId, channelId)
            .orderByAsc(ChannelMember::getJoinTime));
    }
}
