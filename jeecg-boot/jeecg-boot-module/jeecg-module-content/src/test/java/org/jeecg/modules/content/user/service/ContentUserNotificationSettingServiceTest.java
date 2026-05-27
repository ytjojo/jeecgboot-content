package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
import org.jeecg.modules.content.user.mapper.ContentNotificationAuditLogMapper;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentUserNotificationSettingServiceTest {

    @Mock
    private ContentUserNotificationSettingMapper notificationSettingMapper;

    @Mock
    private ContentNotificationAuditLogMapper auditLogMapper;

    @Mock
    private ContentUserSettingsCacheService settingsCacheService;

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

    @Test
    void shouldAutoUpgradeOldDndFormatToMultiPeriod() {
        // 旧单时段 JSON：有 startTime/endTime 但无 dndRules
        ContentUserNotificationSetting setting = ContentUserNotificationSetting.defaults("u1")
            .setDndRuleJson("{\"enabled\":true,\"startTime\":\"22:00\",\"endTime\":\"08:00\"}");
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(setting);

        ContentUserNotificationSettingVO result = notificationSettingService.getSetting("u1");

        // 应自动升级为单元素列表
        assertThat(result.getDndRule().getDndRules()).hasSize(1);
        assertThat(result.getDndRule().getDndRules().get(0).getStartTime()).isEqualTo("22:00");
        assertThat(result.getDndRule().getDndRules().get(0).getEndTime()).isEqualTo("08:00");
        assertThat(result.getDndRule().getDndRules().get(0).getDayType()).isEqualTo("DAILY");
    }

    @Test
    void shouldBlockNoticeDuringMultiPeriodDnd() {
        // 多时段免打扰：22:00-08:00 DAILY
        ContentUserNotificationSetting setting = ContentUserNotificationSetting.defaults("u1")
            .setDndRuleJson("{\"enabled\":true,\"dndRules\":[{\"enabled\":true,\"startTime\":\"22:00\",\"endTime\":\"08:00\",\"dayType\":\"DAILY\",\"summaryMode\":false}]}");
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(setting);

        boolean allowed = notificationSettingService.canSendNotice("u1", "LIKE", "IN_APP", LocalTime.of(23, 30));

        assertThat(allowed).isFalse();
    }

    @Test
    void shouldAllowNoticeOutsideAllDndPeriods() {
        // 多时段免打扰：12:00-13:00 WORKDAY，10:00 不在免打扰时段内
        ContentUserNotificationSetting setting = ContentUserNotificationSetting.defaults("u1")
            .setDndRuleJson("{\"enabled\":true,\"dndRules\":[{\"enabled\":true,\"startTime\":\"12:00\",\"endTime\":\"13:00\",\"dayType\":\"WORKDAY\",\"summaryMode\":false}]}");
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(setting);

        boolean allowed = notificationSettingService.canSendNotice("u1", "LIKE", "IN_APP", LocalTime.of(10, 0));

        assertThat(allowed).isTrue();
    }

    @Test
    void shouldRespectTemporaryDisable() {
        // 免打扰已启用，但临时关闭到未来时间
        long futureTs = System.currentTimeMillis() + 3600_000; // 1小时后
        ContentUserNotificationSetting setting = ContentUserNotificationSetting.defaults("u1")
            .setDndRuleJson("{\"enabled\":true,\"temporaryDisableUntil\":" + futureTs + ",\"dndRules\":[{\"enabled\":true,\"startTime\":\"00:00\",\"endTime\":\"23:59\",\"dayType\":\"DAILY\",\"summaryMode\":false}]}");
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(setting);

        boolean allowed = notificationSettingService.canSendNotice("u1", "LIKE", "IN_APP", LocalTime.of(12, 0));

        // 临时关闭期间应允许发送
        assertThat(allowed).isTrue();
    }

    /** 更新通知设置后应驱逐通知缓存。 */
    @Test
    void shouldEvictNotificationCacheOnSettingUpdate() {
        ContentUserNotificationSetting setting = ContentUserNotificationSetting.defaults("u1");
        setting.setId("setting-1");
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(setting);
        ContentUserNotificationUpdateReq req = new ContentUserNotificationUpdateReq()
            .setCommentNoticeEnabled(Boolean.FALSE);

        notificationSettingService.updateSetting("u1", req);

        verify(settingsCacheService).evictNotification("u1");
    }

    /** 更新免打扰规则后应驱逐通知缓存。 */
    @Test
    void shouldEvictNotificationCacheOnDndRuleUpdate() {
        ContentUserNotificationSetting setting = ContentUserNotificationSetting.defaults("u1");
        setting.setId("setting-1");
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(setting);
        ContentNotificationDndRuleReq req = new ContentNotificationDndRuleReq()
            .setEnabled(Boolean.TRUE)
            .setStartTime("22:00")
            .setEndTime("08:00");

        notificationSettingService.updateDndRule("u1", req);

        verify(settingsCacheService).evictNotification("u1");
    }
}
