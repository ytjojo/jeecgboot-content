package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserFeedSetting;
import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
import org.jeecg.modules.content.user.entity.ContentUserRelationGroup;
import org.jeecg.modules.content.user.mapper.ContentUserFeedSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserNotificationSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationGroupMapper;
import org.jeecg.modules.content.user.service.impl.ContentSocialSubscriptionDefaultsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 内容社区社交订阅默认配置服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentSocialSubscriptionDefaultsServiceTest {

    @Mock
    private ContentUserRelationGroupMapper relationGroupMapper;

    @Mock
    private ContentUserFeedSettingMapper feedSettingMapper;

    @Mock
    private ContentUserNotificationSettingMapper notificationSettingMapper;

    @InjectMocks
    private ContentSocialSubscriptionDefaultsServiceImpl defaultsService;

    @Test
    void shouldCreateMissingDefaultsForNewUser() {
        when(relationGroupMapper.selectOne(any())).thenReturn(null);
        when(feedSettingMapper.selectByUserId("u1")).thenReturn(null);
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(null);

        defaultsService.ensureDefaults("u1");

        verify(relationGroupMapper).insert(argThat((ContentUserRelationGroup it) ->
            "u1".equals(it.getOwnerUserId()) && Boolean.TRUE.equals(it.getIsDefault())
                && "默认分组".equals(it.getGroupName()) && "ACTIVE".equals(it.getGroupStatus())));
        verify(feedSettingMapper).insert(argThat((ContentUserFeedSetting it) ->
            "u1".equals(it.getUserId()) && Boolean.TRUE.equals(it.getPublishEnabled())
                && "PUBLISH,LIKE,FAVORITE".equals(it.getActivityTypes())));
        verify(notificationSettingMapper).insert(argThat((ContentUserNotificationSetting it) ->
            "u1".equals(it.getUserId()) && Boolean.TRUE.equals(it.getSubscriptionNoticeEnabled())
                && "IN_APP,PUSH".equals(it.getSubscriptionDefaultChannels())
                && "REALTIME".equals(it.getSubscriptionDefaultFrequency())));
    }

    @Test
    void shouldNotDuplicateExistingDefaults() {
        when(relationGroupMapper.selectOne(any())).thenReturn(new ContentUserRelationGroup().setIsDefault(Boolean.TRUE));
        when(feedSettingMapper.selectByUserId("u1")).thenReturn(new ContentUserFeedSetting().setUserId("u1"));
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(ContentUserNotificationSetting.defaults("u1"));

        defaultsService.ensureDefaults("u1");

        verify(relationGroupMapper, never()).insert(any(ContentUserRelationGroup.class));
        verify(feedSettingMapper, never()).insert(any(ContentUserFeedSetting.class));
        verify(notificationSettingMapper, never()).insert(any(ContentUserNotificationSetting.class));
        verify(notificationSettingMapper, never()).updateById(any(ContentUserNotificationSetting.class));
    }

    @Test
    void shouldPatchLegacyNotificationDefaultsWithoutOverwritingExistingValues() {
        ContentUserNotificationSetting legacy = new ContentUserNotificationSetting()
            .setUserId("u1")
            .setSubscriptionNoticeEnabled(null)
            .setSubscriptionDefaultChannels("EMAIL")
            .setSubscriptionDefaultFrequency(null);
        when(relationGroupMapper.selectOne(any())).thenReturn(new ContentUserRelationGroup().setIsDefault(Boolean.TRUE));
        when(feedSettingMapper.selectByUserId("u1")).thenReturn(new ContentUserFeedSetting().setUserId("u1"));
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(legacy);

        defaultsService.ensureDefaults("u1");

        verify(notificationSettingMapper).updateById(legacy);
        assertThat(legacy.getSubscriptionNoticeEnabled()).isTrue();
        assertThat(legacy.getSubscriptionDefaultChannels()).isEqualTo("EMAIL");
        assertThat(legacy.getSubscriptionDefaultFrequency()).isEqualTo("REALTIME");
    }
}
