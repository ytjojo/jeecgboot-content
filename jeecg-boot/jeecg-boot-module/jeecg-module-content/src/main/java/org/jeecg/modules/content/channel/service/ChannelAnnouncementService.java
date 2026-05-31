package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;

public interface ChannelAnnouncementService {
    ChannelAnnouncement create(String channelId, String title, String content, String createdBy);
    void update(String id, String title, String content);
    void delete(String id);
    ChannelAnnouncement getByChannelId(String channelId);
}
