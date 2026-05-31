package org.jeecg.modules.content.channel.service;

public interface ChannelContentGovernanceLogService {
    void log(String channelId, String contentId, String operatorId, String action, String detail, String reason, String result);
}
