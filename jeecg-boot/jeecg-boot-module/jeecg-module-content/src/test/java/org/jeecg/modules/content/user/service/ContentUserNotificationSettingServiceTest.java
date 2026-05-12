package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
import org.jeecg.modules.content.user.mapper.ContentUserNotificationSettingMapper;
import org.jeecg.modules.content.user.req.settings.ContentNotificationChannelConfigReq;
import org.jeecg.modules.content.user.req.settings.ContentNotificationDndRuleReq;
import org.jeecg.modules.content.user.req.settings.ContentUserNotificationUpdateReq;
import org.jeecg.modules.content.user.service.impl.ContentUserNotificationSettingServiceImpl;
import org.jeecg.modules.content.user.vo.ContentUserNotificationSettingVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentUserNotificationSettingServiceTest {

    @Mock
    private ContentUserNotificationSettingMapper notificationSettingMapper;

    @InjectMocks
    private ContentUserNotificationSettingServiceImpl notificationSettingService;

    @Test
    void shouldUpdateNotificationChannelsAndDndRule() {
        ContentUserNotificationSetting setting = ContentUserNotificationSetting.defaults("u1");
        setting.setId("setting-1");
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(setting);
        ContentUserNotificationUpdateReq req = new ContentUserNotificationUpdateReq()
            .setCommentNoticeEnabled(Boolean.FALSE)
            .setChannelConfig(new ContentNotificationChannelConfigReq()
                .setLikeChannels(List.of("IN_APP", "EMAIL"))
                .setCommentChannels(List.of("IN_APP")))
            .setDndRule(new ContentNotificationDndRuleReq()
                .setEnabled(Boolean.TRUE)
                .setStartTime("22:00")
                .setEndTime("08:00"));

        ContentUserNotificationSettingVO result = notificationSettingService.updateSetting("u1", req);

        assertThat(result.getCommentNoticeEnabled()).isFalse();
        assertThat(result.getChannelConfig().getLikeChannels()).containsExactly("IN_APP", "EMAIL");
        assertThat(result.getDndRule().getStartTime()).isEqualTo("22:00");
        ArgumentCaptor<ContentUserNotificationSetting> captor = ArgumentCaptor.forClass(ContentUserNotificationSetting.class);
        verify(notificationSettingMapper).updateById(captor.capture());
        assertThat(captor.getValue().getChannelConfigJson()).contains("likeChannels");
        assertThat(captor.getValue().getDndRuleJson()).contains("22:00");
    }

    @Test
    void shouldBlockDisabledNoticeType() {
        ContentUserNotificationSetting setting = ContentUserNotificationSetting.defaults("u1")
            .setCommentNoticeEnabled(Boolean.FALSE);
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(setting);

        boolean allowed = notificationSettingService.canSendNotice("u1", "COMMENT", "IN_APP", LocalTime.of(10, 0));

        assertThat(allowed).isFalse();
    }

    @Test
    void shouldBlockNoticeDuringDndWindow() {
        ContentUserNotificationSetting setting = ContentUserNotificationSetting.defaults("u1")
            .setDndRuleJson("{\"enabled\":true,\"startTime\":\"22:00\",\"endTime\":\"08:00\"}");
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(setting);

        boolean allowed = notificationSettingService.canSendNotice("u1", "LIKE", "IN_APP", LocalTime.of(23, 30));

        assertThat(allowed).isFalse();
    }

    @Test
    void shouldAllowSecurityNoticeEvenWhenDndEnabled() {
        boolean allowed = notificationSettingService.canSendNotice("u1", "SECURITY", "SMS", LocalTime.of(23, 30));

        assertThat(allowed).isTrue();
    }
}
