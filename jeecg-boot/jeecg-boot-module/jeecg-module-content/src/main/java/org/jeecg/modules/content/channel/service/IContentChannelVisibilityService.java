package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.dto.ChannelVisibilityDTO;

import java.util.List;

public interface IContentChannelVisibilityService {

    boolean isDiscoverable(ChannelVisibilityDTO channel);

    List<ChannelVisibilityDTO> filterDiscoverable(List<ChannelVisibilityDTO> channels);
}
