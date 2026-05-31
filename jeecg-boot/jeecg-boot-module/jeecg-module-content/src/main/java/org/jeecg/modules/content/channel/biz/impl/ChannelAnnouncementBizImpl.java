package org.jeecg.modules.content.channel.biz.impl;

import org.jeecg.modules.content.channel.biz.ChannelAnnouncementBiz;
import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;
import org.jeecg.modules.content.channel.req.announcement.ChannelAnnouncementReq;
import org.jeecg.modules.content.channel.service.ChannelAnnouncementService;
import org.jeecg.modules.content.channel.service.ChannelContentGovernanceLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChannelAnnouncementBizImpl implements ChannelAnnouncementBiz {

    @Resource
    private ChannelAnnouncementService announcementService;

    @Resource
    private ChannelContentGovernanceLogService governanceLogService;

    @Override
    public ChannelAnnouncement create(ChannelAnnouncementReq req, String operatorId) {
        ChannelAnnouncement announcement = announcementService.create(req.getChannelId(), req.getTitle(), req.getContent(), operatorId);
        governanceLogService.log(req.getChannelId(), null, operatorId, "ANNOUNCEMENT_CREATE", null, null, "SUCCESS");
        return announcement;
    }

    @Override
    public void update(String id, ChannelAnnouncementReq req) {
        announcementService.update(id, req.getTitle(), req.getContent());
    }

    @Override
    public void delete(String id) {
        announcementService.delete(id);
    }

    @Override
    public ChannelAnnouncement getByChannelId(String channelId) {
        return announcementService.getByChannelId(channelId);
    }
}
