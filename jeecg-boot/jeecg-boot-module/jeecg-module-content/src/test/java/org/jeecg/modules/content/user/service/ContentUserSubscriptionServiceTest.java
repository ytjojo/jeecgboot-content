package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserSubscription;
import org.jeecg.modules.content.user.mapper.ContentUserSubscriptionMapper;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionReq;
import org.jeecg.modules.content.user.service.impl.ContentUserSubscriptionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for content user subscription service.
 */
@ExtendWith(MockitoExtension.class)
class ContentUserSubscriptionServiceTest {

    @Mock
    private ContentUserSubscriptionMapper subscriptionMapper;

    @Mock
    private IContentUserLevelBenefitService levelBenefitService;

    @InjectMocks
    private ContentUserSubscriptionServiceImpl subscriptionService;

    @Test
    void shouldResumeExistingPausedSubscriptionInsteadOfCreatingDuplicate() {
        ContentUserSubscription existing = new ContentUserSubscription()
            .setUserId("u1")
            .setSourceType("TOPIC")
            .setSourceId("topic-1")
            .setSourceName("旧专题")
            .setNotificationChannels("SITE")
            .setNotificationFrequency("DAILY")
            .setPaused(Boolean.TRUE);
        existing.setId("sub-1");
        when(subscriptionMapper.selectByUniqueKey("u1", "TOPIC", "topic-1")).thenReturn(existing);

        var result = subscriptionService.subscribe("u1", new ContentSubscriptionReq()
            .setSourceType("TOPIC")
            .setSourceId("topic-1")
            .setSourceName("新专题")
            .setNotificationChannels("SITE,PUSH")
            .setNotificationFrequency("REALTIME"));

        assertThat(result.getSubscriptionId()).isEqualTo("sub-1");
        assertThat(existing.getPaused()).isFalse();
        assertThat(existing.getSourceName()).isEqualTo("新专题");
        assertThat(existing.getNotificationChannels()).isEqualTo("SITE,PUSH");
        assertThat(existing.getNotificationFrequency()).isEqualTo("REALTIME");
        verify(subscriptionMapper, never()).insert(existing);
        verify(subscriptionMapper).updateById(existing);
    }

    @Test
    void shouldPauseSubscriptionOwnedByCurrentUser() {
        ContentUserSubscription subscription = new ContentUserSubscription()
            .setUserId("u1")
            .setPaused(Boolean.FALSE);
        subscription.setId("sub-2");
        when(subscriptionMapper.selectById("sub-2")).thenReturn(subscription);

        subscriptionService.pauseSubscription("u1", "sub-2");

        assertThat(subscription.getPaused()).isTrue();
        verify(subscriptionMapper).updateById(subscription);
    }

    @Test
    void shouldCancelSubscriptionOwnedByCurrentUser() {
        ContentUserSubscription subscription = new ContentUserSubscription()
            .setUserId("u1");
        when(subscriptionMapper.selectById("sub-1")).thenReturn(subscription);

        var result = subscriptionService.cancelSubscription("u1", "sub-1");

        assertThat(result.getSubscriptionStatus()).isEqualTo("CANCELLED");
        assertThat(subscription.getPaused()).isTrue();
        verify(subscriptionMapper).updateById(subscription);
    }

    @Test
    void shouldRejectCancellingSubscriptionOwnedByAnotherUser() {
        ContentUserSubscription subscription = new ContentUserSubscription()
            .setUserId("u2");
        when(subscriptionMapper.selectById("sub-2")).thenReturn(subscription);

        assertThatThrownBy(() -> subscriptionService.cancelSubscription("u1", "sub-2"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("订阅不存在或无权操作");

        verify(subscriptionMapper, never()).updateById(subscription);
    }

    @Test
    void shouldRejectNewTopicSubscriptionWhenQuotaReached() {
        when(subscriptionMapper.selectByUniqueKey("u1", "TOPIC", "topic-11")).thenReturn(null);
        when(subscriptionMapper.countByUserIdAndSourceType("u1", "TOPIC")).thenReturn(10L);
        when(levelBenefitService.resolveTopicQuota("u1")).thenReturn(10);

        assertThatThrownBy(() -> subscriptionService.subscribe("u1", new ContentSubscriptionReq()
            .setSourceType("TOPIC")
            .setSourceId("topic-11")
            .setSourceName("新话题")))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("当前等级可订阅话题数已达上限");
    }

    @Test
    void shouldAllowTopicSubscriptionWhenExpandedQuotaEnabled() {
        when(subscriptionMapper.selectByUniqueKey("u1", "TOPIC", "topic-21")).thenReturn(null);
        when(subscriptionMapper.countByUserIdAndSourceType("u1", "TOPIC")).thenReturn(20L);
        when(levelBenefitService.resolveTopicQuota("u1")).thenReturn(30);

        var result = subscriptionService.subscribe("u1", new ContentSubscriptionReq()
            .setSourceType("TOPIC")
            .setSourceId("topic-21")
            .setSourceName("增强话题"));

        assertThat(result.getSubscriptionId()).isNotBlank();
        verify(subscriptionMapper).insert(argThat((ContentUserSubscription it) ->
            "u1".equals(it.getUserId()) && "TOPIC".equals(it.getSourceType())));
    }

    @Test
    void shouldUpdateExistingTopicSubscriptionWithoutQuotaFailure() {
        ContentUserSubscription existing = new ContentUserSubscription()
            .setUserId("u1")
            .setSourceType("TOPIC")
            .setSourceId("topic-1")
            .setPaused(Boolean.TRUE);
        existing.setId("sub-1");
        when(subscriptionMapper.selectByUniqueKey("u1", "TOPIC", "topic-1")).thenReturn(existing);

        var result = subscriptionService.subscribe("u1", new ContentSubscriptionReq()
            .setSourceType("TOPIC")
            .setSourceId("topic-1")
            .setSourceName("已存在话题"));

        assertThat(result.getSubscriptionId()).isEqualTo("sub-1");
        verify(subscriptionMapper, never()).countByUserIdAndSourceType("u1", "TOPIC");
    }

    @Test
    void shouldTranslateDuplicateTopicSubscriptionToBusinessError() {
        when(subscriptionMapper.selectByUniqueKey("u1", "TOPIC", "topic-31")).thenReturn(null);
        when(subscriptionMapper.countByUserIdAndSourceType("u1", "TOPIC")).thenReturn(1L);
        when(levelBenefitService.resolveTopicQuota("u1")).thenReturn(10);
        doThrow(new DuplicateKeyException("uk_content_user_subscription"))
            .when(subscriptionMapper).insert(any(ContentUserSubscription.class));

        assertThatThrownBy(() -> subscriptionService.subscribe("u1", new ContentSubscriptionReq()
            .setSourceType("TOPIC")
            .setSourceId("topic-31")
            .setSourceName("并发话题")))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("请勿重复订阅同一订阅源");
    }

    @Test
    void shouldSupportMultipleSourceTypesAndRejectInvalidValues() {
        when(subscriptionMapper.selectByUniqueKey("u1", "CHANNEL", "channel-1")).thenReturn(null);

        var result = subscriptionService.subscribe("u1", new ContentSubscriptionReq()
            .setSourceType("CHANNEL")
            .setSourceId("channel-1")
            .setSourceName("频道"));

        assertThat(result.getSourceType()).isEqualTo("CHANNEL");
        verify(subscriptionMapper).insert(argThat((ContentUserSubscription it) ->
            "ACTIVE".equals(it.getSubscriptionStatus()) && Boolean.FALSE.equals(it.getPaused())));

        assertThatThrownBy(() -> subscriptionService.subscribe("u1", new ContentSubscriptionReq()
                .setSourceType("UNKNOWN")
                .setSourceId("s1")
                .setSourceName("未知源")))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("订阅源类型不支持");
    }

    @Test
    void shouldReturnPagedSubscriptionsWithNotificationSummary() {
        ContentUserSubscription subscription = new ContentUserSubscription()
            .setUserId("u1")
            .setSourceType("TOPIC")
            .setSourceId("topic-1")
            .setSourceName("话题")
            .setNotificationChannels("IN_APP")
            .setNotificationFrequency("DAILY")
            .setPaused(false)
            .setSubscriptionStatus("ACTIVE");
        subscription.setId("sub-1");
        when(subscriptionMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<ContentUserSubscription> page = invocation.getArgument(0);
            page.setRecords(java.util.List.of(subscription));
            page.setTotal(1L);
            return page;
        });

        var result = subscriptionService.listSubscriptions("u1", "TOPIC", 1L, 10L);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords().get(0).getNotificationSummary()).isEqualTo("IN_APP/DAILY");
    }

    @Test
    void shouldReturnSubscriptionFeedOrderedBySourceUpdateTime() {
        Date latestUpdate = new Date();
        ContentUserSubscription subscription = new ContentUserSubscription()
            .setUserId("u1")
            .setSourceType("COLUMN")
            .setSourceId("column-1")
            .setSourceName("专栏")
            .setNotificationChannels("IN_APP")
            .setNotificationFrequency("REALTIME")
            .setLastUpdateTime(latestUpdate)
            .setPaused(false)
            .setSubscriptionStatus("ACTIVE");
        subscription.setId("sub-1");
        when(subscriptionMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<ContentUserSubscription> page = invocation.getArgument(0);
            page.setRecords(java.util.List.of(subscription));
            page.setTotal(11L);
            return page;
        });

        var result = subscriptionService.listSubscriptionFeed("u1", "COLUMN", 1L, 10L);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getSourceType()).isEqualTo("COLUMN");
        assertThat(result.getRecords().get(0).getUpdateTime()).isEqualTo(latestUpdate);
        assertThat(result.getHasMore()).isTrue();
    }

    @Test
    void shouldReturnEmptyFeedWhenCancelledAndPausedSubscriptionsAreFiltered() {
        when(subscriptionMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<ContentUserSubscription> page = invocation.getArgument(0);
            page.setRecords(java.util.List.of());
            page.setTotal(0L);
            return page;
        });

        var result = subscriptionService.listSubscriptionFeed("u1", null, 2L, 10L);

        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getHasMore()).isFalse();
    }

    @Test
    void shouldRejectInvalidSubscriptionFeedFiltersBeforeQuerying() {
        assertThatThrownBy(() -> subscriptionService.listSubscriptionFeed("u1", "UNKNOWN", 1L, 10L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("订阅源类型不支持");
        assertThatThrownBy(() -> subscriptionService.listSubscriptionFeed("u1", "TOPIC", 1L, 101L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("分页大小不能超过100");

        verify(subscriptionMapper, never()).selectPage(any(), any());
    }

    @Test
    void shouldBatchPauseResumeAndCancelWithPartialFailures() {
        ContentUserSubscription owned = new ContentUserSubscription()
            .setUserId("u1")
            .setPaused(false)
            .setSubscriptionStatus("ACTIVE");
        owned.setId("sub-1");
        when(subscriptionMapper.selectById("sub-1")).thenReturn(owned);
        when(subscriptionMapper.selectById("missing")).thenReturn(null);

        var pauseResult = subscriptionService.batchPause("u1", java.util.List.of("sub-1", "missing"));

        assertThat(owned.getPaused()).isTrue();
        assertThat(pauseResult.getSuccessCount()).isEqualTo(1);
        assertThat(pauseResult.getFailureCount()).isEqualTo(1);
        assertThat(pauseResult.getFailures().get(0).getReason()).isEqualTo("订阅不存在或无权操作");

        var resumeResult = subscriptionService.batchResume("u1", java.util.List.of("sub-1"));
        assertThat(owned.getPaused()).isFalse();
        assertThat(resumeResult.getSuccessCount()).isEqualTo(1);

        var cancelResult = subscriptionService.batchCancel("u1", java.util.List.of("sub-1"));
        assertThat(owned.getSubscriptionStatus()).isEqualTo("CANCELLED");
        assertThat(cancelResult.getSuccessCount()).isEqualTo(1);
    }

    @Test
    void shouldRejectInvalidBatchSubscriptionIds() {
        assertThatThrownBy(() -> subscriptionService.batchPause("u1", java.util.List.of()))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("订阅ID列表不能为空");
        assertThatThrownBy(() -> subscriptionService.batchPause("u1", java.util.List.of("sub-1", "sub-1")))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("订阅ID不能重复");

        verify(subscriptionMapper, never()).selectById("sub-1");
    }
}
