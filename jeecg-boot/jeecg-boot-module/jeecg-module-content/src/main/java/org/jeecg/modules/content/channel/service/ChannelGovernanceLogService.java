package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.enums.GovernanceAction;

public interface ChannelGovernanceLogService {

    void log(GovernanceAction action, String channelId, String operatorId, String targetUserId, String reason, String extraData);
}
