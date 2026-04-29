package org.jeecg.modules.content.user.service.impl;

import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserSubscription;
import org.jeecg.modules.content.user.mapper.ContentUserSubscriptionMapper;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionReq;
import org.jeecg.modules.content.user.service.IContentUserSubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ContentUserSubscriptionServiceImpl implements IContentUserSubscriptionService {

    @Resource
    private ContentUserSubscriptionMapper subscriptionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String subscribe(String userId, ContentSubscriptionReq req) {
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSubscription(String userId, String subscriptionId) {
        subscriptionMapper.deleteById(subscriptionId);
    }

    @Override
    public List<ContentUserSubscription> listSubscriptions(String userId) {
        return subscriptionMapper.selectByUserId(userId);
    }
}
