package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelSubscription;
import org.jeecg.modules.content.channel.enums.PrivacyType;
import org.jeecg.modules.content.channel.mapper.ChannelSubscriptionMapper;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.service.impl.ChannelSubscriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * 订阅业务逻辑测试
 * 验证订阅/取消订阅的业务规则：公开频道可直接订阅，私有频道需先加入
 */
@ExtendWith(MockitoExtension.class)
class ChannelSubscriptionBizServiceTest {

    @Mock
    private ChannelSubscriptionMapper subscriptionMapper;

    @Mock
    private ChannelService channelService;

    @InjectMocks
    private ChannelSubscriptionServiceImpl subscriptionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(subscriptionService, "baseMapper", subscriptionMapper);
    }

    @Test
    void should_subscribe_to_public_channel() {
        // 公开频道允许直接订阅，这是内容消费的核心功能
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setPrivacy(PrivacyType.PUBLIC.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);
        when(subscriptionMapper.selectCount(any())).thenReturn(0L);

        subscriptionService.subscribe("ch1", "user1");

        verify(subscriptionMapper).insert(any(ChannelSubscription.class));
    }

    @Test
    void should_reject_subscribe_to_private_channel_for_non_member() {
        // 私有频道非成员不能订阅，需先加入才能订阅
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setPrivacy(PrivacyType.PRIVATE.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);

        assertThatThrownBy(() -> subscriptionService.subscribe("ch1", "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("私有频道需先加入");
    }

    @Test
    void should_unsubscribe_removes_subscription() {
        // 取消订阅应删除订阅记录
        ChannelSubscription sub = new ChannelSubscription();
        sub.setId("sub1");
        when(subscriptionMapper.selectOne(any(), anyBoolean())).thenReturn(sub);

        subscriptionService.unsubscribe("ch1", "user1");

        verify(subscriptionMapper).deleteById("sub1");
    }

    @Test
    void should_reject_duplicate_subscription() {
        // 重复订阅应报错，防止数据冗余
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setPrivacy(PrivacyType.PUBLIC.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);
        when(subscriptionMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> subscriptionService.subscribe("ch1", "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("已订阅");
    }

    @Test
    void should_reject_unsubscribe_when_not_subscribed() {
        // 未订阅时取消订阅应报错
        when(subscriptionMapper.selectOne(any(), anyBoolean())).thenReturn(null);

        assertThatThrownBy(() -> subscriptionService.unsubscribe("ch1", "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("未订阅");
    }

    @Test
    void should_check_subscription_status() {
        // 查询订阅状态应返回正确结果
        when(subscriptionMapper.selectCount(any())).thenReturn(1L);

        assertThat(subscriptionService.isSubscribed("ch1", "user1")).isTrue();
    }

    @Test
    void should_reject_subscribe_when_channel_not_found() {
        // 订阅不存在的频道应报错
        when(channelService.getById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> subscriptionService.subscribe("nonexistent", "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("频道不存在");
    }
}
