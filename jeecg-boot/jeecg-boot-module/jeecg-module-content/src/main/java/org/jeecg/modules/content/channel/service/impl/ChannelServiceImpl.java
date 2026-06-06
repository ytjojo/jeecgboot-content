package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.mapper.ChannelMapper;
import org.jeecg.modules.content.channel.req.ChannelListQuery;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelServiceImpl extends JeecgServiceImpl<ChannelMapper, Channel>
    implements ChannelService {

    private static final List<ChannelStatus> NAME_OCCUPIED_STATUSES = List.of(
        ChannelStatus.PENDING_REVIEW,
        ChannelStatus.ACTIVE,
        ChannelStatus.DELETE_COOLING
    );

    @Override
    public boolean checkNameUnique(String name, String excludeId) {
        LambdaQueryWrapper<Channel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Channel::getName, name)
               .in(Channel::getStatus, NAME_OCCUPIED_STATUSES)
               .ne(Channel::getChannelType, ChannelType.SYSTEM);

        if (excludeId != null) {
            wrapper.ne(Channel::getId, excludeId);
        }

        return baseMapper.selectCount(wrapper) == 0;
    }

    @Override
    public IPage<Channel> listMyChannels(Page<Channel> page, String userId, ChannelListQuery query) {
        LambdaQueryWrapper<Channel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Channel::getOwnerId, userId)
               .ne(Channel::getStatus, ChannelStatus.DELETED);

        if (StringUtils.isNotBlank(query.getChannelType())) {
            ChannelType type = safeParseChannelType(query.getChannelType());
            if (type != null) {
                wrapper.eq(Channel::getChannelType, type);
            }
        }
        if (StringUtils.isNotBlank(query.getStatus())) {
            ChannelStatus status = safeParseChannelStatus(query.getStatus());
            if (status != null) {
                wrapper.eq(Channel::getStatus, status);
            }
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(Channel::getName, query.getKeyword());
        }

        // PendingReview 和 Rejected 置顶，然后按创建时间倒序
        wrapper.orderByDesc(Channel::getStatus, Channel::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Channel> listAllChannels(Page<Channel> page, ChannelListQuery query) {
        LambdaQueryWrapper<Channel> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Channel::getStatus, ChannelStatus.DELETED);

        if (StringUtils.isNotBlank(query.getChannelType())) {
            ChannelType type = safeParseChannelType(query.getChannelType());
            if (type != null) {
                wrapper.eq(Channel::getChannelType, type);
            }
        }
        if (StringUtils.isNotBlank(query.getStatus())) {
            ChannelStatus status = safeParseChannelStatus(query.getStatus());
            if (status != null) {
                wrapper.eq(Channel::getStatus, status);
            }
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(Channel::getName, query.getKeyword());
        }

        // PendingReview 和 Rejected 置顶，然后按创建时间倒序
        wrapper.orderByDesc(Channel::getStatus, Channel::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    private ChannelType safeParseChannelType(String value) {
        try {
            return ChannelType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ChannelStatus safeParseChannelStatus(String value) {
        try {
            return ChannelStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
