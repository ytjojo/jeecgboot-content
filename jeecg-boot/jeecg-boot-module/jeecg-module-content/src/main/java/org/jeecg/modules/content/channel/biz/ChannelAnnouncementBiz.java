package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;
import org.jeecg.modules.content.channel.req.announcement.ChannelAnnouncementReq;

public interface ChannelAnnouncementBiz {
    ChannelAnnouncement create(ChannelAnnouncementReq req, String operatorId);
    void update(String id, ChannelAnnouncementReq req);
    void delete(String id);
    ChannelAnnouncement getByChannelId(String channelId);
}
