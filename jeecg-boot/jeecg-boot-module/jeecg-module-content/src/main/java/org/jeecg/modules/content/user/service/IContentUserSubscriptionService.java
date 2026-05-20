package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionReq;
import org.jeecg.modules.content.user.vo.ContentSubscriptionFeedPageVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionBatchResultVO;
import org.jeecg.modules.content.user.vo.ContentUserSubscriptionPageVO;
import org.jeecg.modules.content.user.vo.ContentUserSubscriptionVO;

/**
 * Service contract for content user subscription.
 */
public interface IContentUserSubscriptionService {

    ContentUserSubscriptionVO subscribe(String userId, ContentSubscriptionReq req);

    ContentUserSubscriptionVO pauseSubscription(String userId, String subscriptionId);

    ContentUserSubscriptionVO resumeSubscription(String userId, String subscriptionId);

    ContentUserSubscriptionVO cancelSubscription(String userId, String subscriptionId);

    ContentUserSubscriptionPageVO listSubscriptions(String userId, String sourceType, Long pageNo, Long pageSize);

    ContentSubscriptionFeedPageVO listSubscriptionFeed(String userId, String sourceType, Long pageNo, Long pageSize);

    ContentSubscriptionBatchResultVO batchPause(String userId, java.util.List<String> subscriptionIds);

    ContentSubscriptionBatchResultVO batchResume(String userId, java.util.List<String> subscriptionIds);

    ContentSubscriptionBatchResultVO batchCancel(String userId, java.util.List<String> subscriptionIds);
}
