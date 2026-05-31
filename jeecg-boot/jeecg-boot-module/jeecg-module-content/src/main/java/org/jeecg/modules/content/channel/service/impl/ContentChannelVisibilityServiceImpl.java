package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.modules.content.channel.dto.ChannelVisibilityDTO;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.service.IContentChannelVisibilityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentChannelVisibilityServiceImpl implements IContentChannelVisibilityService {

    @Override
    public boolean isDiscoverable(ChannelVisibilityDTO channel) {
        if (channel == null) {
            return false;
        }
        // 频道必须处于 Active 状态
        if (channel.getStatus() != ChannelStatus.ACTIVE) {
            return false;
        }
        // 频道必须是公开的
        if (channel.getPrivacy() == null || channel.getPrivacy() != 1) {
            return false;
        }
        return true;
    }

    @Override
    public List<ChannelVisibilityDTO> filterDiscoverable(List<ChannelVisibilityDTO> channels) {
        if (channels == null) {
            return List.of();
        }
        return channels.stream()
                .filter(this::isDiscoverable)
                .collect(Collectors.toList());
    }
}
