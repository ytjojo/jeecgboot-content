package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.biz.impl.ChannelAnnouncementBizImpl;
import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;
import org.jeecg.modules.content.channel.req.announcement.ChannelAnnouncementReq;
import org.jeecg.modules.content.channel.service.ChannelAnnouncementService;
import org.jeecg.modules.content.channel.service.ChannelContentGovernanceLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelAnnouncementBizTest {

    @InjectMocks
    private ChannelAnnouncementBizImpl biz;

    @Mock
    private ChannelAnnouncementService announcementService;

    @Mock
    private ChannelContentGovernanceLogService governanceLogService;

    @Test
    void create_shouldDelegateAndLog() {
        ChannelAnnouncementReq req = new ChannelAnnouncementReq();
        req.setChannelId("ch-1");
        req.setTitle("公告标题");
        req.setContent("公告内容");

        ChannelAnnouncement expected = new ChannelAnnouncement();
        expected.setChannelId("ch-1");
        expected.setTitle("公告标题");
        when(announcementService.create("ch-1", "公告标题", "公告内容", "admin-1")).thenReturn(expected);

        ChannelAnnouncement result = biz.create(req, "admin-1");

        assertSame(expected, result);
        verify(governanceLogService).log(eq("ch-1"), isNull(), eq("admin-1"), eq("ANNOUNCEMENT_CREATE"), isNull(), isNull(), eq("SUCCESS"));
    }

    @Test
    void update_shouldDelegateToService() {
        ChannelAnnouncementReq req = new ChannelAnnouncementReq();
        req.setTitle("新标题");
        req.setContent("新内容");

        biz.update("ann-1", req);

        verify(announcementService).update("ann-1", "新标题", "新内容");
    }

    @Test
    void delete_shouldDelegateToService() {
        biz.delete("ann-1");

        verify(announcementService).delete("ann-1");
    }

    @Test
    void getByChannelId_shouldDelegateToService() {
        ChannelAnnouncement expected = new ChannelAnnouncement();
        expected.setChannelId("ch-1");
        when(announcementService.getByChannelId("ch-1")).thenReturn(expected);

        ChannelAnnouncement result = biz.getByChannelId("ch-1");

        assertSame(expected, result);
    }
}
