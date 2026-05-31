package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;
import org.jeecg.modules.content.channel.mapper.ChannelAnnouncementMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelAnnouncementServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelAnnouncementServiceTest {

    @InjectMocks
    private ChannelAnnouncementServiceImpl service;

    @Mock
    private ChannelAnnouncementMapper announcementMapper;

    @Test
    void create_shouldInsertWithActiveStatus() {
        when(announcementMapper.insert(any(ChannelAnnouncement.class))).thenReturn(1);

        ChannelAnnouncement announcement = service.create("ch-1", "标题", "<p>内容</p>", "admin-1");
        assertEquals("ACTIVE", announcement.getStatus());
        assertEquals("ch-1", announcement.getChannelId());
    }

    @Test
    void delete_shouldMarkAsDeleted() {
        ChannelAnnouncement announcement = new ChannelAnnouncement();
        announcement.setId("ann-1");
        announcement.setStatus("ACTIVE");
        when(announcementMapper.selectById("ann-1")).thenReturn(announcement);
        when(announcementMapper.updateById(any(ChannelAnnouncement.class))).thenReturn(1);

        service.delete("ann-1");
        assertEquals("DELETED", announcement.getStatus());
    }
}
