package org.jeecg.modules.content.channel.service;

import java.util.List;

public interface ChannelBlacklistService {

    void addToBlacklist(String channelId, String userId, String operatorId, String reason);

    void removeFromBlacklist(String channelId, String userId, String operatorId);

    boolean isBlacklisted(String channelId, String userId);

    List<String> listBlacklistedUserIds(String channelId);
}
