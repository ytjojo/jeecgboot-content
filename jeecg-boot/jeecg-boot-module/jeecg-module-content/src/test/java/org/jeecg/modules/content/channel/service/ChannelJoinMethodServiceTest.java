package org.jeecg.modules.content.channel.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.JoinMethod;
import org.jeecg.modules.content.channel.service.impl.ChannelJoinMethodServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelJoinMethodServiceTest {

    @Mock
    private ChannelService channelService;

    @InjectMocks
    private ChannelJoinMethodServiceImpl joinMethodService;

    @Test
    void should_set_join_method_to_review() {
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setJoinMethod(JoinMethod.FREE.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);

        joinMethodService.updateJoinMethod("ch1", JoinMethod.REVIEW, "user1");

        assertThat(channel.getJoinMethod()).isEqualTo(JoinMethod.REVIEW.getCode());
        verify(channelService).updateById(channel);
    }

    @Test
    void should_reject_when_channel_not_found() {
        when(channelService.getById("ch999")).thenReturn(null);

        assertThatThrownBy(() -> joinMethodService.updateJoinMethod("ch999", JoinMethod.REVIEW, "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("频道不存在");
    }
}
