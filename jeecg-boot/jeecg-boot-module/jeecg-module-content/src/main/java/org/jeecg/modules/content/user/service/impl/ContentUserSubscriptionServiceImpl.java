package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserSubscription;
import org.jeecg.modules.content.user.mapper.ContentUserSubscriptionMapper;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionReq;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitService;
import org.jeecg.modules.content.user.service.IContentUserSubscriptionService;
import org.jeecg.modules.content.user.vo.ContentSubscriptionBatchResultVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionFeedItemVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionFeedPageVO;
import org.jeecg.modules.content.user.vo.ContentUserSubscriptionPageVO;
import org.jeecg.modules.content.user.vo.ContentUserSubscriptionVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service implementation for content user subscription.
 */
@Service
public class ContentUserSubscriptionServiceImpl implements IContentUserSubscriptionService {

    private static final String SOURCE_TYPE_TOPIC = "TOPIC";
    private static final int USER_ID_MAX_LENGTH = 64;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 100L;
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String CANCELLED_STATUS = "CANCELLED";
    private static final Set<String> SUPPORTED_SOURCE_TYPES =
        Set.of("TOPIC", "TAG", "COLLECTION", "SPECIAL", "COLUMN", "CHANNEL");

    @Resource
    private ContentUserSubscriptionMapper subscriptionMapper;

    @Resource
    private IContentUserLevelBenefitService levelBenefitService;

    /**
     * Creates a subscription for the requested content source.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserSubscriptionVO subscribe(String userId, ContentSubscriptionReq req) {
        requireValidUserId(userId);
        validateSubscriptionReq(req);
        ContentUserSubscription existingSubscription =
            subscriptionMapper.selectByUniqueKey(userId, req.getSourceType(), req.getSourceId());
        if (existingSubscription != null) {
            existingSubscription.setSourceName(req.getSourceName());
            existingSubscription.setNotificationChannels(req.getNotificationChannels());
            existingSubscription.setNotificationFrequency(req.getNotificationFrequency());
            existingSubscription.setPaused(Boolean.FALSE);
            existingSubscription.setSubscriptionStatus(ACTIVE_STATUS);
            subscriptionMapper.updateById(existingSubscription);
            return ContentUserSubscriptionVO.from(existingSubscription);
        }
        validateTopicQuotaIfNecessary(userId, req);
        ContentUserSubscription subscription = new ContentUserSubscription();
        subscription.setId(UUIDGenerator.generate());
        subscription.setUserId(userId);
        subscription.setSourceType(req.getSourceType());
        subscription.setSourceId(req.getSourceId());
        subscription.setSourceName(req.getSourceName());
        subscription.setNotificationChannels(req.getNotificationChannels());
        subscription.setNotificationFrequency(req.getNotificationFrequency());
        subscription.setPaused(Boolean.FALSE);
        subscription.setSubscriptionStatus(ACTIVE_STATUS);
        subscription.setSubscribedAt(new Date());
        try {
            subscriptionMapper.insert(subscription);
        } catch (DuplicateKeyException ex) {
            throw new JeecgBootException("请勿重复订阅同一订阅源");
        }
        return ContentUserSubscriptionVO.from(subscription);
    }

    /**
     * Pauses the specified subscription record.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserSubscriptionVO pauseSubscription(String userId, String subscriptionId) {
        ContentUserSubscription subscription = getOwnedSubscription(userId, subscriptionId);
        subscription.setPaused(Boolean.TRUE);
        subscriptionMapper.updateById(subscription);
        return ContentUserSubscriptionVO.from(subscription);
    }

    /**
     * Resumes the specified subscription record.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserSubscriptionVO resumeSubscription(String userId, String subscriptionId) {
        ContentUserSubscription subscription = getOwnedSubscription(userId, subscriptionId);
        subscription.setPaused(Boolean.FALSE);
        subscription.setSubscriptionStatus(ACTIVE_STATUS);
        subscriptionMapper.updateById(subscription);
        return ContentUserSubscriptionVO.from(subscription);
    }

    /**
     * Cancels the specified subscription record.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserSubscriptionVO cancelSubscription(String userId, String subscriptionId) {
        ContentUserSubscription subscription = getOwnedSubscription(userId, subscriptionId);
        subscription.setSubscriptionStatus(CANCELLED_STATUS);
        subscription.setPaused(Boolean.TRUE);
        subscriptionMapper.updateById(subscription);
        return ContentUserSubscriptionVO.from(subscription);
    }

    /**
     * Lists all subscriptions owned by the target user.
     */
    @Override
    public ContentUserSubscriptionPageVO listSubscriptions(String userId, String sourceType, Long pageNo, Long pageSize) {
        requireValidUserId(userId);
        if (sourceType != null && !sourceType.trim().isEmpty()) {
            validateSourceType(sourceType);
        }
        long currentPage = normalizePageNo(pageNo);
        long currentSize = normalizePageSize(pageSize);
        Page<ContentUserSubscription> page = subscriptionMapper.selectPage(new Page<>(currentPage, currentSize),
            Wrappers.<ContentUserSubscription>lambdaQuery()
                .eq(ContentUserSubscription::getUserId, userId)
                .ne(ContentUserSubscription::getSubscriptionStatus, CANCELLED_STATUS)
                .eq(sourceType != null && !sourceType.trim().isEmpty(), ContentUserSubscription::getSourceType, sourceType)
                .orderByDesc(ContentUserSubscription::getSubscribedAt)
                .orderByDesc(ContentUserSubscription::getUpdateTime));
        return new ContentUserSubscriptionPageVO()
            .setRecords(page.getRecords().stream().map(ContentUserSubscriptionVO::from).toList())
            .setTotal(page.getTotal())
            .setPageNo(currentPage)
            .setPageSize(currentSize);
    }

    /**
     * 查询当前用户订阅源的最新更新流。
     */
    @Override
    public ContentSubscriptionFeedPageVO listSubscriptionFeed(String userId, String sourceType, Long pageNo, Long pageSize) {
        requireValidUserId(userId);
        if (sourceType != null && !sourceType.trim().isEmpty()) {
            validateSourceType(sourceType);
        }
        long currentPage = normalizePageNo(pageNo);
        long currentSize = normalizePageSize(pageSize);
        Page<ContentUserSubscription> page = subscriptionMapper.selectPage(new Page<>(currentPage, currentSize),
            Wrappers.<ContentUserSubscription>lambdaQuery()
                .eq(ContentUserSubscription::getUserId, userId)
                .eq(ContentUserSubscription::getSubscriptionStatus, ACTIVE_STATUS)
                .eq(ContentUserSubscription::getPaused, Boolean.FALSE)
                .eq(sourceType != null && !sourceType.trim().isEmpty(), ContentUserSubscription::getSourceType, sourceType)
                .orderByDesc(ContentUserSubscription::getLastUpdateTime)
                .orderByDesc(ContentUserSubscription::getUpdateTime)
                .orderByDesc(ContentUserSubscription::getId));
        return new ContentSubscriptionFeedPageVO()
            .setRecords(page.getRecords().stream().map(ContentSubscriptionFeedItemVO::from).toList())
            .setTotal(page.getTotal())
            .setPageNo(currentPage)
            .setPageSize(currentSize)
            .setHasMore(currentPage * currentSize < page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentSubscriptionBatchResultVO batchPause(String userId, List<String> subscriptionIds) {
        return batchUpdate(userId, subscriptionIds, "PAUSE");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentSubscriptionBatchResultVO batchResume(String userId, List<String> subscriptionIds) {
        return batchUpdate(userId, subscriptionIds, "RESUME");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentSubscriptionBatchResultVO batchCancel(String userId, List<String> subscriptionIds) {
        return batchUpdate(userId, subscriptionIds, "CANCEL");
    }

    private void validateTopicQuotaIfNecessary(String userId, ContentSubscriptionReq req) {
        if (req == null || !SOURCE_TYPE_TOPIC.equals(req.getSourceType()) || levelBenefitService == null) {
            return;
        }
        int topicQuota = levelBenefitService.resolveTopicQuota(userId);
        Long currentCount = subscriptionMapper.countByUserIdAndSourceType(userId, SOURCE_TYPE_TOPIC);
        if (currentCount != null && currentCount >= topicQuota) {
            throw new JeecgBootException("当前等级可订阅话题数已达上限");
        }
    }

    private ContentUserSubscription getOwnedSubscription(String userId, String subscriptionId) {
        requireValidUserId(userId);
        validateSubscriptionId(subscriptionId);
        ContentUserSubscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null || !userId.equals(subscription.getUserId())) {
            throw new JeecgBootException("订阅不存在或无权操作");
        }
        return subscription;
    }

    private ContentSubscriptionBatchResultVO batchUpdate(String userId, List<String> subscriptionIds, String operation) {
        requireValidUserId(userId);
        validateBatchSubscriptionIds(subscriptionIds);
        ContentSubscriptionBatchResultVO result = new ContentSubscriptionBatchResultVO();
        for (String subscriptionId : subscriptionIds) {
            ContentUserSubscription subscription = subscriptionMapper.selectById(subscriptionId);
            if (subscription == null || !userId.equals(subscription.getUserId())) {
                result.addFailure(subscriptionId, "订阅不存在或无权操作");
                continue;
            }
            if ("PAUSE".equals(operation)) {
                subscription.setPaused(Boolean.TRUE);
            } else if ("RESUME".equals(operation)) {
                subscription.setPaused(Boolean.FALSE);
                subscription.setSubscriptionStatus(ACTIVE_STATUS);
            } else {
                subscription.setPaused(Boolean.TRUE);
                subscription.setSubscriptionStatus(CANCELLED_STATUS);
            }
            subscriptionMapper.updateById(subscription);
            result.addSuccess();
        }
        return result;
    }

    private void validateSubscriptionReq(ContentSubscriptionReq req) {
        if (req == null) {
            throw new JeecgBootException("订阅请求不能为空");
        }
        validateSourceType(req.getSourceType());
        if (req.getSourceId() == null || req.getSourceId().trim().isEmpty()) {
            throw new JeecgBootException("订阅源ID不能为空");
        }
        if (req.getSourceId().length() > 64) {
            throw new JeecgBootException("订阅源ID长度不能超过64位");
        }
        if (req.getSourceName() == null || req.getSourceName().trim().isEmpty()) {
            throw new JeecgBootException("订阅源名称不能为空");
        }
        if (req.getSourceName().length() > 128) {
            throw new JeecgBootException("订阅源名称长度不能超过128位");
        }
    }

    private void validateSourceType(String sourceType) {
        if (sourceType == null || sourceType.trim().isEmpty()) {
            throw new JeecgBootException("订阅源类型不能为空");
        }
        if (sourceType.length() > 32) {
            throw new JeecgBootException("订阅源类型长度不能超过32位");
        }
        if (!SUPPORTED_SOURCE_TYPES.contains(sourceType)) {
            throw new JeecgBootException("订阅源类型不支持");
        }
    }

    private void requireValidUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new JeecgBootException("用户ID不能为空");
        }
        if (userId.length() > USER_ID_MAX_LENGTH) {
            throw new JeecgBootException("用户ID长度不能超过64位");
        }
    }

    private void validateSubscriptionId(String subscriptionId) {
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            throw new JeecgBootException("订阅ID不能为空");
        }
        if (subscriptionId.length() > USER_ID_MAX_LENGTH) {
            throw new JeecgBootException("订阅ID长度不能超过64位");
        }
    }

    private void validateBatchSubscriptionIds(List<String> subscriptionIds) {
        if (subscriptionIds == null || subscriptionIds.isEmpty()) {
            throw new JeecgBootException("订阅ID列表不能为空");
        }
        if (subscriptionIds.size() > 100) {
            throw new JeecgBootException("订阅ID数量不能超过100个");
        }
        Set<String> seen = new HashSet<>();
        for (String subscriptionId : subscriptionIds) {
            validateSubscriptionId(subscriptionId);
            if (!seen.add(subscriptionId)) {
                throw new JeecgBootException("订阅ID不能重复");
            }
        }
    }

    private long normalizePageNo(Long pageNo) {
        return pageNo == null || pageNo < 1L ? 1L : pageNo;
    }

    private long normalizePageSize(Long pageSize) {
        long currentSize = pageSize == null || pageSize < 1L ? DEFAULT_PAGE_SIZE : pageSize;
        if (currentSize > MAX_PAGE_SIZE) {
            throw new JeecgBootException("分页大小不能超过100");
        }
        return currentSize;
    }
}
