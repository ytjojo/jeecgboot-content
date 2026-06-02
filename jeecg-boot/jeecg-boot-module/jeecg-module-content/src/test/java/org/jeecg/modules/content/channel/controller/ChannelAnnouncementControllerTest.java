package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelAnnouncementBiz;
import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;
import org.jeecg.modules.content.channel.req.announcement.ChannelAnnouncementReq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道公告控制器测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelAnnouncementControllerTest {

    @Mock
    private ChannelAnnouncementBiz channelAnnouncementBiz;

    @InjectMocks
    private ChannelAnnouncementController controller;

    @BeforeEach
    void setUp() {
        LoginUser user = new LoginUser();
        user.setId("user1");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
            JSON.toJSONString(user), null));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_create_announcement() {
        ChannelAnnouncementReq req = new ChannelAnnouncementReq();
        ChannelAnnouncement created = new ChannelAnnouncement();
        when(channelAnnouncementBiz.create(req, "user1")).thenReturn(created);

        Result<?> result = controller.create(req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelAnnouncementBiz).create(req, "user1");
    }

    @Test
    void should_update_announcement() {
        ChannelAnnouncementReq req = new ChannelAnnouncementReq();

        Result<Void> result = controller.update("a1", req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelAnnouncementBiz).update("a1", req);
    }

    @Test
    void should_delete_announcement() {
        Result<Void> result = controller.delete("a1");

        assertThat(result.isSuccess()).isTrue();
        verify(channelAnnouncementBiz).delete("a1");
    }

    @Test
    void should_get_announcement_by_channel() {
        ChannelAnnouncement found = new ChannelAnnouncement();
        when(channelAnnouncementBiz.getByChannelId("ch1")).thenReturn(found);

        Result<?> result = controller.getByChannelId("ch1");

        assertThat(result.isSuccess()).isTrue();
        verify(channelAnnouncementBiz).getByChannelId("ch1");
    }
}
