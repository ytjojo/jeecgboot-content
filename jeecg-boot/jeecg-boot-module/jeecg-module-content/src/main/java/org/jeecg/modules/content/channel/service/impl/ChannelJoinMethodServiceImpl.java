package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.JoinMethod;
import org.jeecg.modules.content.channel.service.ChannelJoinMethodService;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class ChannelJoinMethodServiceImpl implements ChannelJoinMethodService {

    @Resource
    private ChannelService channelService;

    @Override
    public void updateJoinMethod(String channelId, JoinMethod joinMethod, String operatorId) {
        Channel channel = channelService.getById(channelId);
        if (channel == null) {
            throw new JeecgBootException("频道不存在");
        }
        channel.setJoinMethod(joinMethod.getCode());
        channelService.updateById(channel);
    }
}
