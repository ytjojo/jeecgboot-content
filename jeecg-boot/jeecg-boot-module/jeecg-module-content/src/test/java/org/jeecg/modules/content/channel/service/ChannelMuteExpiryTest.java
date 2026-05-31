package org.jeecg.modules.content.channel.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelMute;
import org.jeecg.modules.content.channel.mapper.ChannelMuteMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelMuteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 禁言过期测试
 * 验证禁言记录的创建、状态检查、过期判断、手动解除
 */
@ExtendWith(MockitoExtension.class)
class ChannelMuteExpiryTest {

    @Mock
    private ChannelMuteMapper muteMapper;

    @InjectMocks
    private ChannelMuteServiceImpl muteService;

    @Test
    void should_create_mute_record() {
        // 禁言时应创建包含频道、用户、时长等信息的记录
        muteService.mute("ch1", "user1", "admin1", "违规发言", 7);

        verify(muteMapper).insert(any(ChannelMute.class));
    }

    @Test
    void should_return_true_for_active_mute() {
        // 未过期且未解除的禁言应返回 true
        ChannelMute mute = new ChannelMute();
        mute.setEndTime(new Date(System.currentTimeMillis() + 86400000 * 3));
        when(muteMapper.selectOne(any())).thenReturn(mute);

        assertThat(muteService.isMuted("ch1", "user1")).isTrue();
    }

    @Test
    void should_return_false_for_expired_mute() {
        // 已过期的禁言应返回 false，允许用户发言
        ChannelMute mute = new ChannelMute();
        mute.setEndTime(new Date(System.currentTimeMillis() - 86400000));
        when(muteMapper.selectOne(any())).thenReturn(mute);

        assertThat(muteService.isMuted("ch1", "user1")).isFalse();
    }

    @Test
    void should_return_true_for_permanent_mute() {
        // 永久禁言（endTime为null）应始终返回 true
        ChannelMute mute = new ChannelMute();
        mute.setEndTime(null);
        when(muteMapper.selectOne(any())).thenReturn(mute);

        assertThat(muteService.isMuted("ch1", "user1")).isTrue();
    }

    @Test
    void should_return_false_when_no_mute_record() {
        // 没有禁言记录时应返回 false
        when(muteMapper.selectOne(any())).thenReturn(null);

        assertThat(muteService.isMuted("ch1", "user1")).isFalse();
    }

    @Test
    void should_unmute_sets_unmute_type() {
        // 手动解除禁言应设置 unmuteType=2 和 unmuteTime
        ChannelMute mute = new ChannelMute();
        mute.setId("mute1");
        when(muteMapper.selectOne(any())).thenReturn(mute);

        muteService.unmute("ch1", "user1", "admin1");

        assertThat(mute.getUnmuteType()).isEqualTo(2);
        assertThat(mute.getUnmuteTime()).isNotNull();
        verify(muteMapper).updateById(mute);
    }

    @Test
    void should_reject_unmute_when_no_active_mute() {
        // 没有有效禁言记录时解除禁言应报错
        when(muteMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> muteService.unmute("ch1", "user1", "admin1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("未找到有效禁言记录");
    }
}
