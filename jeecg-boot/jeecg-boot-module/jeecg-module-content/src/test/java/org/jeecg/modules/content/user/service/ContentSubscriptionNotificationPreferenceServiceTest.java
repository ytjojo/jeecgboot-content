package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentSubscriptionNotificationPreference;
import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
import org.jeecg.modules.content.user.entity.ContentUserSubscription;
import org.jeecg.modules.content.user.mapper.ContentSubscriptionNotificationPreferenceMapper;
import org.jeecg.modules.content.user.mapper.ContentUserNotificationSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserSubscriptionMapper;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionNotificationPreferenceReq;
import org.jeecg.modules.content.user.service.impl.ContentSubscriptionNotificationPreferenceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

/**
 * 订阅通知偏好服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentSubscriptionNotificationPreferenceServiceTest {

    @Mock
    private ContentSubscriptionNotificationPreferenceMapper preferenceMapper;

    @Mock
    private ContentUserSubscriptionMapper subscriptionMapper;

    @Mock
    private ContentUserNotificationSettingMapper notificationSettingMapper;

    @InjectMocks
    private ContentSubscriptionNotificationPreferenceServiceImpl preferenceService;

    @Test
    void shouldSaveSubscriptionNotificationPreference() {
        when(subscriptionMapper.selectById("sub-1")).thenReturn(subscription("sub-1", "u1"));
        when(preferenceMapper.selectBySubscriptionId("sub-1")).thenReturn(null);

        var result = preferenceService.savePreference("u1", new ContentSubscriptionNotificationPreferenceReq()
            .setSubscriptionId("sub-1")
            .setNotificationChannels(List.of("IN_APP", "email"))
            .setNotificationFrequency("daily")
            .setDndStartTime("22:00")
            .setDndEndTime("07:00"));

        assertThat(result.getNotificationChannels()).containsExactly("IN_APP", "EMAIL");
        assertThat(result.getNotificationFrequency()).isEqualTo("DAILY");
        assertThat(result.getInherited()).isFalse();
        verify(preferenceMapper).insert(any(ContentSubscriptionNotificationPreference.class));
        verify(preferenceMapper).updateById(any(ContentSubscriptionNotificationPreference.class));
    }

    @Test
    void shouldRejectInvalidPreferenceValuesBeforeSaving() {
        when(subscriptionMapper.selectById("sub-1")).thenReturn(subscription("sub-1", "u1"));

        assertThatThrownBy(() -> preferenceService.savePreference("u1", new ContentSubscriptionNotificationPreferenceReq()
                .setSubscriptionId("sub-1")
                .setNotificationChannels(List.of())
                .setNotificationFrequency("REALTIME")))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("通知渠道不能为空");
        assertThatThrownBy(() -> preferenceService.savePreference("u1", new ContentSubscriptionNotificationPreferenceReq()
                .setSubscriptionId("sub-1")
                .setNotificationChannels(List.of("SMS"))
                .setNotificationFrequency("REALTIME")))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("通知渠道不支持");
        assertThatThrownBy(() -> preferenceService.savePreference("u1", new ContentSubscriptionNotificationPreferenceReq()
                .setSubscriptionId("sub-1")
                .setNotificationChannels(List.of("IN_APP"))
                .setNotificationFrequency("WEEKLY")))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("通知频率不支持");
        assertThatThrownBy(() -> preferenceService.savePreference("u1", new ContentSubscriptionNotificationPreferenceReq()
                .setSubscriptionId("sub-1")
                .setNotificationChannels(List.of("IN_APP"))
                .setNotificationFrequency("REALTIME")
                .setDndStartTime("22:00")
                .setDndEndTime("22:00")))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("免打扰开始和结束时间不能相同");

        verify(preferenceMapper, never()).updateById(any(ContentSubscriptionNotificationPreference.class));
    }

    @Test
    void shouldInheritGlobalDefaultsWhenSubscriptionPreferenceMissing() {
        when(subscriptionMapper.selectById("sub-1")).thenReturn(subscription("sub-1", "u1"));
        when(preferenceMapper.selectBySubscriptionId("sub-1")).thenReturn(null);
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(new ContentUserNotificationSetting()
            .setUserId("u1")
            .setSubscriptionNoticeEnabled(true)
            .setSubscriptionDefaultChannels("EMAIL")
            .setSubscriptionDefaultFrequency("DAILY"));

        var result = preferenceService.getEffectivePreference("u1", "sub-1");

        assertThat(result.getInherited()).isTrue();
        assertThat(result.getNotificationChannels()).containsExactly("EMAIL");
        assertThat(result.getNotificationFrequency()).isEqualTo("DAILY");
    }

    @Test
    void shouldBuildNotificationDecisionForDailySummary() {
        when(subscriptionMapper.selectById("sub-1")).thenReturn(subscription("sub-1", "u1"));
        when(preferenceMapper.selectBySubscriptionId("sub-1")).thenReturn(new ContentSubscriptionNotificationPreference()
            .setSubscriptionId("sub-1")
            .setUserId("u1")
            .setNotificationChannels("EMAIL")
            .setNotificationFrequency("DAILY")
            .setPreferenceStatus("ACTIVE"));

        var result = preferenceService.decideUpdateNotification("u1", "sub-1", "article-1");

        assertThat(result.getRealtimeDelivery()).isFalse();
        assertThat(result.getDailySummary()).isTrue();
        assertThat(result.getUpdateBizId()).isEqualTo("article-1");
    }

    private ContentUserSubscription subscription(String subscriptionId, String userId) {
        ContentUserSubscription subscription = new ContentUserSubscription().setUserId(userId);
        subscription.setId(subscriptionId);
        return subscription;
    }
}
