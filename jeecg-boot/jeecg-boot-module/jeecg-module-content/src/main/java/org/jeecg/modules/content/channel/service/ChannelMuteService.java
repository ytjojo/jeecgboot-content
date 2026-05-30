package org.jeecg.modules.content.channel.service;

public interface ChannelMuteService {

    void mute(String channelId, String userId, String operatorId, String reason, int days);

    void unmute(String channelId, String userId, String operatorId);

    boolean isMuted(String channelId, String userId);
}
