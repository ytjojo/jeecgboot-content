package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;
import org.jeecg.modules.content.channel.mapper.ChannelAnnouncementMapper;
import org.jeecg.modules.content.channel.service.ChannelAnnouncementService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChannelAnnouncementServiceImpl implements ChannelAnnouncementService {

    @Resource
    private ChannelAnnouncementMapper announcementMapper;

    @Override
    public ChannelAnnouncement create(String channelId, String title, String content, String createdBy) {
        ChannelAnnouncement announcement = new ChannelAnnouncement();
        announcement.setChannelId(channelId);
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setCreatedBy(createdBy);
        announcement.setStatus("ACTIVE");
        announcementMapper.insert(announcement);
        return announcement;
    }

    @Override
    public void update(String id, String title, String content) {
        ChannelAnnouncement announcement = announcementMapper.selectById(id);
        announcement.setTitle(title);
        announcement.setContent(content);
        announcementMapper.updateById(announcement);
    }

    @Override
    public void delete(String id) {
        ChannelAnnouncement announcement = announcementMapper.selectById(id);
        announcement.setStatus("DELETED");
        announcementMapper.updateById(announcement);
    }

    @Override
    public ChannelAnnouncement getByChannelId(String channelId) {
        LambdaQueryWrapper<ChannelAnnouncement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelAnnouncement::getChannelId, channelId)
               .eq(ChannelAnnouncement::getStatus, "ACTIVE");
        return announcementMapper.selectOne(wrapper);
    }
}
