package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import static org.mockito.Mockito.*;

/**
 * 社交订阅默认配置服务测试。
 * 验证默认分组、关注流设置和通知设置的幂等补齐逻辑。
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
    void shouldCreateAllDefaultsWhenNoneExist() {
        // Given - all mappers return null (no existing defaults)
        String userId = "user001";
        when(relationGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(feedSettingMapper.selectByUserId(userId)).thenReturn(null);
        when(notificationSettingMapper.selectByUserId(userId)).thenReturn(null);
        when(relationGroupMapper.insert(any(ContentUserRelationGroup.class))).thenReturn(1);
        when(feedSettingMapper.insert(any(ContentUserFeedSetting.class))).thenReturn(1);
        when(notificationSettingMapper.insert(any(ContentUserNotificationSetting.class))).thenReturn(1);

        // When
        defaultsService.ensureDefaults(userId);

        // Then - verify 3 inserts
        verify(relationGroupMapper).insert(any(ContentUserRelationGroup.class));
        verify(feedSettingMapper).insert(any(ContentUserFeedSetting.class));
        verify(notificationSettingMapper).insert(any(ContentUserNotificationSetting.class));
    }

    @Test
    void shouldSkipCreationWhenAllDefaultsExist() {
        // Given - all mappers return existing objects
        String userId = "user002";
        ContentUserRelationGroup existingGroup = new ContentUserRelationGroup();
        existingGroup.setId("group001");
        when(relationGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingGroup);

        ContentUserFeedSetting existingFeedSetting = new ContentUserFeedSetting();
        existingFeedSetting.setId("feed001");
        when(feedSettingMapper.selectByUserId(userId)).thenReturn(existingFeedSetting);

        ContentUserNotificationSetting existingNotificationSetting = new ContentUserNotificationSetting();
        existingNotificationSetting.setId("notif001");
        existingNotificationSetting.setSubscriptionNoticeEnabled(true);
        existingNotificationSetting.setSubscriptionDefaultChannels("IN_APP,PUSH");
        existingNotificationSetting.setSubscriptionDefaultFrequency("REALTIME");
        when(notificationSettingMapper.selectByUserId(userId)).thenReturn(existingNotificationSetting);

        // When
        defaultsService.ensureDefaults(userId);

        // Then - 0 inserts, 0 updates
        verify(relationGroupMapper, never()).insert(any(ContentUserRelationGroup.class));
        verify(feedSettingMapper, never()).insert(any(ContentUserFeedSetting.class));
        verify(notificationSettingMapper, never()).insert(any(ContentUserNotificationSetting.class));
        verify(notificationSettingMapper, never()).updateById(any(ContentUserNotificationSetting.class));
    }

    @Test
    void shouldBackFillNotificationSettingFields() {
        // Given - notification setting exists with null subscriptionNoticeEnabled
        String userId = "user003";
        when(relationGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new ContentUserRelationGroup());
        when(feedSettingMapper.selectByUserId(userId)).thenReturn(new ContentUserFeedSetting());

        ContentUserNotificationSetting existingSetting = new ContentUserNotificationSetting();
        existingSetting.setId("notif002");
        existingSetting.setSubscriptionNoticeEnabled(null);
        existingSetting.setSubscriptionDefaultChannels(null);
        existingSetting.setSubscriptionDefaultFrequency(null);
        when(notificationSettingMapper.selectByUserId(userId)).thenReturn(existingSetting);
        when(notificationSettingMapper.updateById(any(ContentUserNotificationSetting.class))).thenReturn(1);

        // When
        defaultsService.ensureDefaults(userId);

        // Then - updateById called
        verify(notificationSettingMapper).updateById(any(ContentUserNotificationSetting.class));
    }

    @Test
    void shouldNotUpdateNotificationSettingWhenAllFieldsPresent() {
        // Given - notification setting fully populated
        String userId = "user004";
        when(relationGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new ContentUserRelationGroup());
        when(feedSettingMapper.selectByUserId(userId)).thenReturn(new ContentUserFeedSetting());

        ContentUserNotificationSetting fullyPopulated = new ContentUserNotificationSetting();
        fullyPopulated.setId("notif003");
        fullyPopulated.setSubscriptionNoticeEnabled(true);
        fullyPopulated.setSubscriptionDefaultChannels("IN_APP,PUSH");
        fullyPopulated.setSubscriptionDefaultFrequency("REALTIME");
        when(notificationSettingMapper.selectByUserId(userId)).thenReturn(fullyPopulated);

        // When
        defaultsService.ensureDefaults(userId);

        // Then - updateById NOT called
        verify(notificationSettingMapper, never()).updateById(any(ContentUserNotificationSetting.class));
    }
}
