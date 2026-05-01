package org.jeecg.modules.content.user.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserSubscription;
import org.jeecg.modules.content.user.mapper.ContentUserSubscriptionMapper;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionReq;
import org.jeecg.modules.content.user.service.IContentUserSubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * Service implementation for content user subscription.
 */
@Service
public class ContentUserSubscriptionServiceImpl implements IContentUserSubscriptionService {

    @Resource
    private ContentUserSubscriptionMapper subscriptionMapper;

    /**
     * Creates a subscription for the requested content source.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String subscribe(String userId, ContentSubscriptionReq req) {
        ContentUserSubscription existingSubscription =
            subscriptionMapper.selectByUniqueKey(userId, req.getSourceType(), req.getSourceId());
        if (existingSubscription != null) {
            existingSubscription.setSourceName(req.getSourceName());
            existingSubscription.setNotificationChannels(req.getNotificationChannels());
            existingSubscription.setNotificationFrequency(req.getNotificationFrequency());
            existingSubscription.setPaused(Boolean.FALSE);
            subscriptionMapper.updateById(existingSubscription);
            return existingSubscription.getId();
        }
        ContentUserSubscription subscription = new ContentUserSubscription();
        subscription.setId(UUIDGenerator.generate());
        subscription.setUserId(userId);
        subscription.setSourceType(req.getSourceType());
        subscription.setSourceId(req.getSourceId());
        subscription.setSourceName(req.getSourceName());
        subscription.setNotificationChannels(req.getNotificationChannels());
        subscription.setNotificationFrequency(req.getNotificationFrequency());
        subscription.setPaused(Boolean.FALSE);
        subscriptionMapper.insert(subscription);
        return subscription.getId();
    }

    /**
     * Pauses the specified subscription record.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseSubscription(String userId, String subscriptionId) {
        ContentUserSubscription subscription = getOwnedSubscription(userId, subscriptionId);
        subscription.setPaused(Boolean.TRUE);
        subscriptionMapper.updateById(subscription);
    }

    /**
     * Resumes the specified subscription record.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resumeSubscription(String userId, String subscriptionId) {
        ContentUserSubscription subscription = getOwnedSubscription(userId, subscriptionId);
        subscription.setPaused(Boolean.FALSE);
        subscriptionMapper.updateById(subscription);
    }

    /**
     * Cancels the specified subscription record.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSubscription(String userId, String subscriptionId) {
        ContentUserSubscription subscription = getOwnedSubscription(userId, subscriptionId);
        subscriptionMapper.deleteById(subscriptionId);
    }

    /**
     * Lists all subscriptions owned by the target user.
     */
    @Override
    public List<ContentUserSubscription> listSubscriptions(String userId) {
        return subscriptionMapper.selectByUserId(userId);
    }

    private ContentUserSubscription getOwnedSubscription(String userId, String subscriptionId) {
        ContentUserSubscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null || !userId.equals(subscription.getUserId())) {
            throw new JeecgBootException("订阅不存在或无权取消");
        }
        return subscription;
    }
}
