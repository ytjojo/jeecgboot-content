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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentUserSubscriptionServiceTest {

    @Mock
    private ContentUserSubscriptionMapper subscriptionMapper;

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

        String subscriptionId = subscriptionService.subscribe("u1", new ContentSubscriptionReq()
            .setSourceType("TOPIC")
            .setSourceId("topic-1")
            .setSourceName("新专题")
            .setNotificationChannels("SITE,PUSH")
            .setNotificationFrequency("REALTIME"));

        assertThat(subscriptionId).isEqualTo("sub-1");
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

        subscriptionService.cancelSubscription("u1", "sub-1");

        verify(subscriptionMapper).deleteById("sub-1");
    }

    @Test
    void shouldRejectCancellingSubscriptionOwnedByAnotherUser() {
        ContentUserSubscription subscription = new ContentUserSubscription()
            .setUserId("u2");
        when(subscriptionMapper.selectById("sub-2")).thenReturn(subscription);

        assertThatThrownBy(() -> subscriptionService.cancelSubscription("u1", "sub-2"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("订阅不存在或无权取消");

        verify(subscriptionMapper, never()).deleteById("sub-2");
    }
}
