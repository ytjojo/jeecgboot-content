package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.enums.PrivacyType;
import org.jeecg.modules.content.channel.service.ChannelPrivacyService;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class ChannelPrivacyServiceImpl implements ChannelPrivacyService {

    @Resource
    private ChannelService channelService;

    @Override
    public void updatePrivacy(String channelId, PrivacyType privacyType, String operatorId) {
        Channel channel = channelService.getById(channelId);
        if (channel == null) {
            throw new JeecgBootException("频道不存在");
        }
        if (channel.getChannelType() == ChannelType.SYSTEM
            && privacyType == PrivacyType.PRIVATE) {
            throw new JeecgBootException("系统频道必须公开");
        }
        channel.setPrivacy(privacyType.getCode());
        channelService.updateById(channel);
    }
}
