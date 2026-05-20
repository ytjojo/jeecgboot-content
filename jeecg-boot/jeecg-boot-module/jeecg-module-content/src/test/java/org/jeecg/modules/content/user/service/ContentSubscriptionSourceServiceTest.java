package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.constant.ContentUserErrorCode;
import org.jeecg.modules.content.user.entity.ContentSubscriptionSource;
import org.jeecg.modules.content.user.entity.ContentUserSubscription;
import org.jeecg.modules.content.user.mapper.ContentSubscriptionSourceMapper;
import org.jeecg.modules.content.user.mapper.ContentUserSubscriptionMapper;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionReq;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionSourceReq;
import org.jeecg.modules.content.user.service.impl.ContentSubscriptionSourceServiceImpl;
import org.jeecg.modules.content.user.vo.ContentUserSubscriptionVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订阅源目录服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentSubscriptionSourceServiceTest {

    @Mock
    private ContentSubscriptionSourceMapper sourceMapper;

    @Mock
    private ContentUserSubscriptionMapper subscriptionMapper;

    @Mock
    private IContentUserSubscriptionService subscriptionService;

    @InjectMocks
    private ContentSubscriptionSourceServiceImpl sourceService;

    @Test
    void shouldSaveNewSourceAndRefreshHeatFields() {
        Date latestUpdate = new Date();
        when(sourceMapper.selectBySource("TOPIC", "topic-1")).thenReturn(null);

        var saved = sourceService.saveSource(new ContentSubscriptionSourceReq()
            .setSourceType("TOPIC")
            .setSourceId("topic-1")
            .setSourceName("话题")
            .setCategory("科技")
            .setSubscriberCount(12)
            .setHeatScore(new BigDecimal("9.8"))
            .setLatestUpdateTime(latestUpdate));

        assertThat(saved.getSourceName()).isEqualTo("话题");
        verify(sourceMapper).insert(argThat((ContentSubscriptionSource it) ->
            Boolean.TRUE.equals(it.getEnabled()) && new BigDecimal("9.8").compareTo(it.getHeatScore()) == 0));

        ContentSubscriptionSource source = enabledSource();
        when(sourceMapper.selectBySource("TOPIC", "topic-1")).thenReturn(source);
        Date refreshedTime = new Date(latestUpdate.getTime() + 1000L);

        var refreshed = sourceService.refreshSource("TOPIC", "topic-1", 20, new BigDecimal("18.5"), refreshedTime);

        assertThat(refreshed.getSubscriberCount()).isEqualTo(20);
        assertThat(refreshed.getLatestUpdateTime()).isEqualTo(refreshedTime);
        verify(sourceMapper).updateById(source);
    }

    @Test
    void shouldRejectInvalidSourceDirectoryValuesAndHideDisabledSources() {
        assertThatThrownBy(() -> sourceService.saveSource(new ContentSubscriptionSourceReq()
                .setSourceType("UNKNOWN")
                .setSourceId("topic-1")
                .setSourceName("话题")))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("订阅源类型不支持")
            .extracting("errCode")
            .isEqualTo(ContentUserErrorCode.SOCIAL_SUBSCRIPTION_SOURCE_INVALID);
        assertThatThrownBy(() -> sourceService.saveSource(new ContentSubscriptionSourceReq()
                .setSourceType("TOPIC")
                .setSourceId("")
                .setSourceName("话题")))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("订阅源ID不能为空");

        ContentSubscriptionSource disabled = enabledSource().setEnabled(Boolean.FALSE);
        when(sourceMapper.selectBySource("TOPIC", "topic-1")).thenReturn(disabled);
        assertThatThrownBy(() -> sourceService.getSourceDetail("u1", "TOPIC", "topic-1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("订阅源不存在或未启用")
            .extracting("errCode")
            .isEqualTo(ContentUserErrorCode.SOCIAL_SUBSCRIPTION_SOURCE_NOT_FOUND);

        verify(sourceMapper, never()).insert(any(ContentSubscriptionSource.class));
    }

    @Test
    void shouldListPlazaWithCategorySearchTypeAndSubscribedState() {
        ContentSubscriptionSource source = enabledSource().setCategory("科技");
        ContentUserSubscription subscription = new ContentUserSubscription()
            .setUserId("u1")
            .setSourceType("TOPIC")
            .setSourceId("topic-1")
            .setSourceName("话题")
            .setSubscriptionStatus("ACTIVE")
            .setPaused(Boolean.FALSE);
        subscription.setId("sub-1");
        when(sourceMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<ContentSubscriptionSource> page = invocation.getArgument(0);
            page.setRecords(java.util.List.of(source));
            page.setTotal(1L);
            return page;
        });
        when(subscriptionMapper.selectByUniqueKey("u1", "TOPIC", "topic-1")).thenReturn(subscription);

        var page = sourceService.listPlaza("u1", "科技", "话题", "TOPIC", 1L, 10L);

        assertThat(page.getTotal()).isEqualTo(1L);
        assertThat(page.getRecords().get(0).getSubscribed()).isTrue();
        assertThat(page.getRecords().get(0).getSubscriptionId()).isEqualTo("sub-1");
    }

    @Test
    void shouldRejectInvalidPlazaFiltersBeforeQuerying() {
        assertThatThrownBy(() -> sourceService.listPlaza("u1", null, null, "UNKNOWN", 1L, 10L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("订阅源类型不支持");
        assertThatThrownBy(() -> sourceService.listPlaza("u1", null, "x".repeat(65), null, 1L, 10L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("搜索关键词长度不能超过64位");
        assertThatThrownBy(() -> sourceService.listPlaza("u1", null, null, null, 1L, 101L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("分页大小不能超过100");

        verify(sourceMapper, never()).selectPage(any(), any());
    }

    @Test
    void shouldReturnSourceDetailWithSubscriptionState() {
        ContentSubscriptionSource source = enabledSource();
        ContentUserSubscription subscription = new ContentUserSubscription()
            .setUserId("u1")
            .setSourceType("TOPIC")
            .setSourceId("topic-1")
            .setSourceName("话题")
            .setSubscriptionStatus("ACTIVE")
            .setPaused(Boolean.TRUE);
        subscription.setId("sub-1");
        when(sourceMapper.selectBySource("TOPIC", "topic-1")).thenReturn(source);
        when(subscriptionMapper.selectByUniqueKey("u1", "TOPIC", "topic-1")).thenReturn(subscription);

        var detail = sourceService.getSourceDetail("u1", "TOPIC", "topic-1");

        assertThat(detail.getSourceDescription()).isEqualTo("介绍");
        assertThat(detail.getSubscribed()).isTrue();
        assertThat(detail.getPaused()).isTrue();
    }

    @Test
    void shouldSubscribeFromPlazaAndReturnSubscriptionState() {
        ContentSubscriptionSource source = enabledSource();
        when(sourceMapper.selectBySource("TOPIC", "topic-1")).thenReturn(source);
        when(subscriptionService.subscribe(argThat("u1"::equals), argThat((ContentSubscriptionReq req) ->
            "TOPIC".equals(req.getSourceType()) && "topic-1".equals(req.getSourceId()) && "话题".equals(req.getSourceName()))))
            .thenReturn(new ContentUserSubscriptionVO()
                .setSubscriptionId("sub-1")
                .setSourceType("TOPIC")
                .setSourceId("topic-1")
                .setSourceName("话题")
                .setSubscriptionStatus("ACTIVE"));

        var subscription = sourceService.subscribeFromPlaza("u1", "TOPIC", "topic-1");

        assertThat(subscription.getSubscriptionId()).isEqualTo("sub-1");
        assertThat(subscription.getSubscriptionStatus()).isEqualTo("ACTIVE");
    }

    private ContentSubscriptionSource enabledSource() {
        ContentSubscriptionSource source = new ContentSubscriptionSource()
            .setSourceType("TOPIC")
            .setSourceId("topic-1")
            .setSourceName("话题")
            .setSourceDescription("介绍")
            .setSubscriberCount(10)
            .setHeatScore(new BigDecimal("8.5"))
            .setLatestUpdateTime(new Date())
            .setEnabled(Boolean.TRUE);
        source.setId("source-1");
        return source;
    }
}
