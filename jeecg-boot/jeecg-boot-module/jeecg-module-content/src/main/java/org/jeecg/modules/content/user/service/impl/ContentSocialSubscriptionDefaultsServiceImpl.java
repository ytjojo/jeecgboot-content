package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserFeedSetting;
import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
import org.jeecg.modules.content.user.entity.ContentUserRelationGroup;
import org.jeecg.modules.content.user.mapper.ContentUserFeedSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserNotificationSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationGroupMapper;
import org.jeecg.modules.content.user.service.IContentSocialSubscriptionDefaultsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

/**
 * 内容社区社交订阅默认配置服务实现。
 */
@Service
public class ContentSocialSubscriptionDefaultsServiceImpl implements IContentSocialSubscriptionDefaultsService {

    private static final String DEFAULT_GROUP_NAME = "默认分组";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DEFAULT_ACTIVITY_TYPES = "PUBLISH,LIKE,FAVORITE";
    private static final String DEFAULT_SUBSCRIPTION_CHANNELS = "IN_APP,PUSH";
    private static final String DEFAULT_SUBSCRIPTION_FREQUENCY = "REALTIME";

    @Resource
    private ContentUserRelationGroupMapper relationGroupMapper;

    @Resource
    private ContentUserFeedSettingMapper feedSettingMapper;

    @Resource
    private ContentUserNotificationSettingMapper notificationSettingMapper;

    /**
     * 幂等补齐用户的默认关注分组、关注流设置和订阅通知默认值。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ensureDefaults(String userId) {
        ensureDefaultRelationGroup(userId);
        ensureDefaultFeedSetting(userId);
        ensureDefaultNotificationSetting(userId);
    }

    private void ensureDefaultRelationGroup(String userId) {
        ContentUserRelationGroup defaultGroup = relationGroupMapper.selectOne(
            Wrappers.<ContentUserRelationGroup>lambdaQuery()
                .eq(ContentUserRelationGroup::getOwnerUserId, userId)
                .eq(ContentUserRelationGroup::getIsDefault, Boolean.TRUE)
                .last("limit 1"));
        if (defaultGroup != null) {
            return;
        }
        ContentUserRelationGroup group = new ContentUserRelationGroup()
            .setOwnerUserId(userId)
            .setGroupName(DEFAULT_GROUP_NAME)
            .setSortOrder(0)
            .setIsDefault(Boolean.TRUE)
            .setGroupStatus(ACTIVE_STATUS);
        group.setId(UUIDGenerator.generate());
        relationGroupMapper.insert(group);
    }

    private void ensureDefaultFeedSetting(String userId) {
        if (feedSettingMapper.selectByUserId(userId) != null) {
            return;
        }
        ContentUserFeedSetting setting = new ContentUserFeedSetting()
            .setUserId(userId)
            .setPublishEnabled(Boolean.TRUE)
            .setLikeEnabled(Boolean.TRUE)
            .setFavoriteEnabled(Boolean.TRUE)
            .setActivityTypes(DEFAULT_ACTIVITY_TYPES);
        setting.setId(UUIDGenerator.generate());
        feedSettingMapper.insert(setting);
    }

    private void ensureDefaultNotificationSetting(String userId) {
        ContentUserNotificationSetting setting = notificationSettingMapper.selectByUserId(userId);
        if (setting == null) {
            setting = ContentUserNotificationSetting.defaults(userId);
            setting.setId(UUIDGenerator.generate());
            notificationSettingMapper.insert(setting);
            return;
        }
        boolean changed = false;
        if (setting.getSubscriptionNoticeEnabled() == null) {
            setting.setSubscriptionNoticeEnabled(Boolean.TRUE);
            changed = true;
        }
        if (setting.getSubscriptionDefaultChannels() == null || setting.getSubscriptionDefaultChannels().isBlank()) {
            setting.setSubscriptionDefaultChannels(DEFAULT_SUBSCRIPTION_CHANNELS);
            changed = true;
        }
        if (setting.getSubscriptionDefaultFrequency() == null || setting.getSubscriptionDefaultFrequency().isBlank()) {
            setting.setSubscriptionDefaultFrequency(DEFAULT_SUBSCRIPTION_FREQUENCY);
            changed = true;
        }
        if (changed) {
            notificationSettingMapper.updateById(setting);
        }
    }
}
