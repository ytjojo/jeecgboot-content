package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelRecycleBin;

public interface ChannelRecycleBinService {
    ChannelRecycleBin addToRecycleBin(String channelId, String contentId, String contentType, String authorId, String deletedBy, String reason);
    boolean restore(String recycleBinId, String restoredBy);
}
