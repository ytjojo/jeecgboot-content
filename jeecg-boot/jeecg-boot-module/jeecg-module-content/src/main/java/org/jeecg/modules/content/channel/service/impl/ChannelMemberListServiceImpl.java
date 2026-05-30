package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.mapper.ChannelMemberMapper;
import org.jeecg.modules.content.channel.service.ChannelMemberListService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class ChannelMemberListServiceImpl implements ChannelMemberListService {

    @Resource
    private ChannelMemberMapper memberMapper;

    @Override
    public IPage<ChannelMember> listMembers(String channelId, Integer role, int pageNum, int pageSize) {
        LambdaQueryWrapper<ChannelMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelMember::getChannelId, channelId);
        if (role != null) {
            wrapper.eq(ChannelMember::getRole, role);
        }
        wrapper.orderByAsc(ChannelMember::getJoinTime);
        return memberMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public IPage<ChannelMember> searchMembers(String channelId, String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<ChannelMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelMember::getChannelId, channelId);
        // Note: nickname search would require join with user table
        wrapper.orderByAsc(ChannelMember::getJoinTime);
        return memberMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
