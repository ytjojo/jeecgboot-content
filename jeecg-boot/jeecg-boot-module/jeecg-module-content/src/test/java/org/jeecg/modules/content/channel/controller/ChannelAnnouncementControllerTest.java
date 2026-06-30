package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelAnnouncementBiz;
import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.mapper.ChannelAnnouncementMapper;
import org.jeecg.modules.content.channel.req.announcement.ChannelAnnouncementReq;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelAnnouncementControllerTest {

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_USERNAME = "testuser";

    @Mock
    private ChannelAnnouncementBiz channelAnnouncementBiz;
    @Mock
    private ChannelMemberService memberService;
    @Mock
    private ChannelAnnouncementMapper announcementMapper;

    @InjectMocks
    private ChannelAnnouncementController controller;

    @BeforeEach
    void setUp() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(TEST_USER_ID);
        loginUser.setUsername(TEST_USERNAME);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                JSON.toJSONString(loginUser), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ChannelMember adminMember = new ChannelMember();
        adminMember.setRole(MemberRole.ADMIN.getCode());
        lenient().when(memberService.getByChannelAndUser(any(), eq(TEST_USER_ID))).thenReturn(adminMember);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_create_announcement() {
        ChannelAnnouncementReq req = new ChannelAnnouncementReq();
        req.setChannelId("ch1");
        ChannelAnnouncement created = new ChannelAnnouncement();
        when(channelAnnouncementBiz.create(req, TEST_USER_ID)).thenReturn(created);

        Result<?> result = controller.create(req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelAnnouncementBiz).create(req, TEST_USER_ID);
    }

    @Test
    void should_update_announcement() {
        ChannelAnnouncementReq req = new ChannelAnnouncementReq();
        req.setChannelId("ch1");

        Result<Void> result = controller.update("a1", req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelAnnouncementBiz).update("a1", req);
    }

    @Test
    void should_delete_announcement() {
        ChannelAnnouncement announcement = new ChannelAnnouncement();
        announcement.setChannelId("ch1");
        when(announcementMapper.selectById("a1")).thenReturn(announcement);

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
