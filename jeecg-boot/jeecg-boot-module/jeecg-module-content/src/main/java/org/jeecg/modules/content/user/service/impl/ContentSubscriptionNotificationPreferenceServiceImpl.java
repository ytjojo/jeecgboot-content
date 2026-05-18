package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentSubscriptionNotificationPreference;
import org.jeecg.modules.content.user.mapper.ContentSubscriptionNotificationPreferenceMapper;
import org.jeecg.modules.content.user.service.IContentSubscriptionNotificationPreferenceService;
import org.springframework.stereotype.Service;

/**
 * 内容社区订阅通知偏好服务实现。
 */
@Service
public class ContentSubscriptionNotificationPreferenceServiceImpl
    extends ServiceImpl<ContentSubscriptionNotificationPreferenceMapper, ContentSubscriptionNotificationPreference>
    implements IContentSubscriptionNotificationPreferenceService {
}
