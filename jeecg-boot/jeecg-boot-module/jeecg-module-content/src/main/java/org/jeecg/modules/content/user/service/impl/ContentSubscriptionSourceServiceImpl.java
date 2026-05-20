package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.constant.ContentUserErrorCode;
import org.jeecg.modules.content.user.entity.ContentSubscriptionSource;
import org.jeecg.modules.content.user.entity.ContentUserSubscription;
import org.jeecg.modules.content.user.mapper.ContentSubscriptionSourceMapper;
import org.jeecg.modules.content.user.mapper.ContentUserSubscriptionMapper;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionReq;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionSourceReq;
import org.jeecg.modules.content.user.service.IContentSubscriptionSourceService;
import org.jeecg.modules.content.user.service.IContentUserSubscriptionService;
import org.jeecg.modules.content.user.vo.ContentSubscriptionSourceDetailVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionSourcePageVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionSourceVO;
import org.jeecg.modules.content.user.vo.ContentUserSubscriptionVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * 内容社区订阅源目录服务实现。
 */
@Service
public class ContentSubscriptionSourceServiceImpl
    extends ServiceImpl<ContentSubscriptionSourceMapper, ContentSubscriptionSource>
    implements IContentSubscriptionSourceService {

    private static final int USER_ID_MAX_LENGTH = 64;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 100L;
    private static final String CANCELLED_STATUS = "CANCELLED";
    private static final Set<String> SUPPORTED_SOURCE_TYPES =
        Set.of("TOPIC", "TAG", "COLLECTION", "SPECIAL", "COLUMN", "CHANNEL");

    @Resource
    private ContentSubscriptionSourceMapper sourceMapper;

    @Resource
    private ContentUserSubscriptionMapper subscriptionMapper;

    @Resource
    private IContentUserSubscriptionService subscriptionService;

    /**
     * 写入或更新订阅源目录快照。
     */
    @Override
    public ContentSubscriptionSourceVO saveSource(ContentSubscriptionSourceReq req) {
        validateSourceReq(req);
        ContentSubscriptionSource source = sourceMapper.selectBySource(req.getSourceType(), req.getSourceId());
        boolean newSource = source == null;
        if (source == null) {
            source = new ContentSubscriptionSource();
            source.setId(UUIDGenerator.generate());
        }
        source.setSourceType(req.getSourceType());
        source.setSourceId(req.getSourceId());
        source.setSourceName(req.getSourceName());
        source.setSourceDescription(req.getSourceDescription());
        source.setCategory(req.getCategory());
        source.setCoverUrl(req.getCoverUrl());
        source.setSubscriberCount(req.getSubscriberCount() == null ? 0 : req.getSubscriberCount());
        source.setHeatScore(req.getHeatScore() == null ? BigDecimal.ZERO : req.getHeatScore());
        source.setLatestUpdateTime(req.getLatestUpdateTime());
        source.setEnabled(req.getEnabled() == null ? Boolean.TRUE : req.getEnabled());
        if (newSource) {
            sourceMapper.insert(source);
        } else {
            sourceMapper.updateById(source);
        }
        return ContentSubscriptionSourceVO.from(source, null);
    }

    /**
     * 刷新订阅源热度和最近更新时间。
     */
    @Override
    public ContentSubscriptionSourceVO refreshSource(String sourceType, String sourceId, Integer subscriberCount,
                                                     BigDecimal heatScore, Date latestUpdateTime) {
        ContentSubscriptionSource source = getEnabledSource(sourceType, sourceId);
        if (subscriberCount != null) {
            source.setSubscriberCount(Math.max(0, subscriberCount));
        }
        if (heatScore != null) {
            source.setHeatScore(heatScore);
        }
        if (latestUpdateTime != null) {
            source.setLatestUpdateTime(latestUpdateTime);
        }
        sourceMapper.updateById(source);
        return ContentSubscriptionSourceVO.from(source, null);
    }

    /**
     * 查询订阅广场列表，默认按热度和最近更新排序。
     */
    @Override
    public ContentSubscriptionSourcePageVO listPlaza(String userId, String category, String keyword, String sourceType,
                                                     Long pageNo, Long pageSize) {
        requireValidUserId(userId);
        if (sourceType != null && !sourceType.trim().isEmpty()) {
            validateSourceType(sourceType);
        }
        validateOptionalText(category, 64, "订阅源分类长度不能超过64位");
        validateOptionalText(keyword, 64, "搜索关键词长度不能超过64位");
        long currentPage = normalizePageNo(pageNo);
        long currentSize = normalizePageSize(pageSize);
        boolean hasCategory = category != null && !category.trim().isEmpty();
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        Page<ContentSubscriptionSource> page = sourceMapper.selectPage(new Page<>(currentPage, currentSize),
            Wrappers.<ContentSubscriptionSource>lambdaQuery()
                .eq(ContentSubscriptionSource::getEnabled, Boolean.TRUE)
                .eq(sourceType != null && !sourceType.trim().isEmpty(), ContentSubscriptionSource::getSourceType, sourceType)
                .eq(hasCategory, ContentSubscriptionSource::getCategory, category)
                .and(hasKeyword, it -> it.like(ContentSubscriptionSource::getSourceName, keyword)
                    .or()
                    .like(ContentSubscriptionSource::getSourceDescription, keyword))
                .orderByDesc(ContentSubscriptionSource::getHeatScore)
                .orderByDesc(ContentSubscriptionSource::getLatestUpdateTime)
                .orderByDesc(ContentSubscriptionSource::getId));
        return new ContentSubscriptionSourcePageVO()
            .setRecords(page.getRecords().stream()
                .map(source -> ContentSubscriptionSourceVO.from(source, getSubscription(userId, source)))
                .toList())
            .setTotal(page.getTotal())
            .setPageNo(currentPage)
            .setPageSize(currentSize);
    }

    /**
     * 查询订阅源详情并回显当前订阅状态。
     */
    @Override
    public ContentSubscriptionSourceDetailVO getSourceDetail(String userId, String sourceType, String sourceId) {
        requireValidUserId(userId);
        ContentSubscriptionSource source = getEnabledSource(sourceType, sourceId);
        return ContentSubscriptionSourceDetailVO.from(source, getSubscription(userId, source));
    }

    /**
     * 从订阅广场详情直接订阅。
     */
    @Override
    public ContentUserSubscriptionVO subscribeFromPlaza(String userId, String sourceType, String sourceId) {
        requireValidUserId(userId);
        ContentSubscriptionSource source = getEnabledSource(sourceType, sourceId);
        return subscriptionService.subscribe(userId, new ContentSubscriptionReq()
            .setSourceType(source.getSourceType())
            .setSourceId(source.getSourceId())
            .setSourceName(source.getSourceName()));
    }

    private ContentUserSubscriptionVO getSubscription(String userId, ContentSubscriptionSource source) {
        ContentUserSubscription subscription =
            subscriptionMapper.selectByUniqueKey(userId, source.getSourceType(), source.getSourceId());
        if (subscription == null || CANCELLED_STATUS.equals(subscription.getSubscriptionStatus())) {
            return null;
        }
        return ContentUserSubscriptionVO.from(subscription);
    }

    private ContentSubscriptionSource getEnabledSource(String sourceType, String sourceId) {
        validateSourceType(sourceType);
        validateRequiredText(sourceId, 64, "订阅源ID不能为空", "订阅源ID长度不能超过64位");
        ContentSubscriptionSource source = sourceMapper.selectBySource(sourceType, sourceId);
        if (source == null || !Boolean.TRUE.equals(source.getEnabled())) {
            throw new JeecgBootException("订阅源不存在或未启用", ContentUserErrorCode.SOCIAL_SUBSCRIPTION_SOURCE_NOT_FOUND);
        }
        return source;
    }

    private void validateSourceReq(ContentSubscriptionSourceReq req) {
        if (req == null) {
            throw invalidSource("订阅源请求不能为空");
        }
        validateSourceType(req.getSourceType());
        validateRequiredText(req.getSourceId(), 64, "订阅源ID不能为空", "订阅源ID长度不能超过64位");
        validateRequiredText(req.getSourceName(), 128, "订阅源名称不能为空", "订阅源名称长度不能超过128位");
        validateOptionalText(req.getSourceDescription(), 512, "订阅源介绍长度不能超过512位");
        validateOptionalText(req.getCategory(), 64, "订阅源分类长度不能超过64位");
        validateOptionalText(req.getCoverUrl(), 255, "封面地址长度不能超过255位");
        if (req.getSubscriberCount() != null && req.getSubscriberCount() < 0) {
            throw invalidSource("订阅人数不能小于0");
        }
    }

    private void validateSourceType(String sourceType) {
        validateRequiredText(sourceType, 32, "订阅源类型不能为空", "订阅源类型长度不能超过32位");
        if (!SUPPORTED_SOURCE_TYPES.contains(sourceType)) {
            throw invalidSource("订阅源类型不支持");
        }
    }

    private void requireValidUserId(String userId) {
        validateRequiredText(userId, USER_ID_MAX_LENGTH, "用户ID不能为空", "用户ID长度不能超过64位");
    }

    private void validateRequiredText(String value, int maxLength, String emptyMessage, String tooLongMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw invalidSource(emptyMessage);
        }
        if (value.length() > maxLength) {
            throw invalidSource(tooLongMessage);
        }
    }

    private void validateOptionalText(String value, int maxLength, String tooLongMessage) {
        if (value != null && value.length() > maxLength) {
            throw invalidSource(tooLongMessage);
        }
    }

    private long normalizePageNo(Long pageNo) {
        return pageNo == null || pageNo < 1L ? 1L : pageNo;
    }

    private long normalizePageSize(Long pageSize) {
        long currentSize = pageSize == null || pageSize < 1L ? DEFAULT_PAGE_SIZE : pageSize;
        if (currentSize > MAX_PAGE_SIZE) {
            throw invalidSource("分页大小不能超过100");
        }
        return currentSize;
    }

    private JeecgBootException invalidSource(String message) {
        return new JeecgBootException(message, ContentUserErrorCode.SOCIAL_SUBSCRIPTION_SOURCE_INVALID);
    }
}
