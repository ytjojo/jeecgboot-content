package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserSubscription;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionReq;

import java.util.List;

/**
 * Service contract for content user subscription.
 */
public interface IContentUserSubscriptionService {

    String subscribe(String userId, ContentSubscriptionReq req);

    void pauseSubscription(String userId, String subscriptionId);

    void resumeSubscription(String userId, String subscriptionId);

    void cancelSubscription(String userId, String subscriptionId);

    List<ContentUserSubscription> listSubscriptions(String userId);
}
