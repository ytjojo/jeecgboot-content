package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.user.entity.ContentSubscriptionNotificationPreference;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionNotificationPreferenceReq;
import org.jeecg.modules.content.user.vo.ContentSubscriptionNotificationDecisionVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionNotificationPreferenceVO;

/**
 * 内容社区订阅通知偏好服务契约。
 */
public interface IContentSubscriptionNotificationPreferenceService extends IService<ContentSubscriptionNotificationPreference> {

    ContentSubscriptionNotificationPreferenceVO savePreference(String userId, ContentSubscriptionNotificationPreferenceReq req);

    ContentSubscriptionNotificationPreferenceVO getEffectivePreference(String userId, String subscriptionId);

    ContentSubscriptionNotificationDecisionVO decideUpdateNotification(String userId, String subscriptionId, String updateBizId);
}
