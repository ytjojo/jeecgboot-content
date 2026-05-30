package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelSubscription;
import org.jeecg.modules.content.channel.enums.PrivacyType;
import org.jeecg.modules.content.channel.mapper.ChannelSubscriptionMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelSubscriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelSubscriptionServiceTest {

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
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setPrivacy(PrivacyType.PUBLIC.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);
        when(subscriptionMapper.selectCount(any())).thenReturn(0L);

        subscriptionService.subscribe("ch1", "user1");

        verify(subscriptionMapper).insert(any(ChannelSubscription.class));
    }

    @Test
    void should_reject_subscribe_to_private_channel() {
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setPrivacy(PrivacyType.PRIVATE.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);

        assertThatThrownBy(() -> subscriptionService.subscribe("ch1", "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("私有频道需先加入");
    }

    @Test
    void should_reject_duplicate_subscription() {
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setPrivacy(PrivacyType.PUBLIC.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);
        when(subscriptionMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> subscriptionService.subscribe("ch1", "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("已订阅");
    }
}
