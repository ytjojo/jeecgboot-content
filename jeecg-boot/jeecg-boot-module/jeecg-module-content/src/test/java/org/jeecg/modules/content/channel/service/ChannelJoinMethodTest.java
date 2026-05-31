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

/**
 * 加入方式配置测试
 * 验证频道加入方式（自由/审核/邀请）的切换逻辑
 */
@ExtendWith(MockitoExtension.class)
class ChannelJoinMethodTest {

    @Mock
    private ChannelService channelService;

    @InjectMocks
    private ChannelJoinMethodServiceImpl joinMethodService;

    @Test
    void should_set_join_method_to_review() {
        // 审核加入模式：用户需提交申请，管理员审批后才能加入
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setJoinMethod(JoinMethod.FREE.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);

        joinMethodService.updateJoinMethod("ch1", JoinMethod.REVIEW, "user1");

        assertThat(channel.getJoinMethod()).isEqualTo(JoinMethod.REVIEW.getCode());
        verify(channelService).updateById(channel);
    }

    @Test
    void should_set_join_method_to_free() {
        // 自由加入模式：任何用户可直接加入频道
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setJoinMethod(JoinMethod.REVIEW.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);

        joinMethodService.updateJoinMethod("ch1", JoinMethod.FREE, "user1");

        assertThat(channel.getJoinMethod()).isEqualTo(JoinMethod.FREE.getCode());
        verify(channelService).updateById(channel);
    }

    @Test
    void should_set_join_method_to_invite() {
        // 邀请加入模式：只能通过邀请码加入，最严格的加入控制
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setJoinMethod(JoinMethod.FREE.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);

        joinMethodService.updateJoinMethod("ch1", JoinMethod.INVITE, "user1");

        assertThat(channel.getJoinMethod()).isEqualTo(JoinMethod.INVITE.getCode());
        verify(channelService).updateById(channel);
    }

    @Test
    void should_throw_when_channel_not_found() {
        // 操作不存在的频道应明确报错
        when(channelService.getById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> joinMethodService.updateJoinMethod("nonexistent", JoinMethod.REVIEW, "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("频道不存在");
    }
}
