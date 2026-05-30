package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.enums.JoinMethod;

public interface ChannelJoinMethodService {

    void updateJoinMethod(String channelId, JoinMethod joinMethod, String operatorId);
}
