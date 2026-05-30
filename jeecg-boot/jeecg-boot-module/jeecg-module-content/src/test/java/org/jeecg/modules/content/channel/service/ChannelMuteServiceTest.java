package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

@ExtendWith(MockitoExtension.class)
class ChannelMuteServiceTest {

    @Mock
    private ChannelMuteMapper muteMapper;

    @InjectMocks
    private ChannelMuteServiceImpl muteService;

    @Test
    void should_mute_member() {
        muteService.mute("ch1", "user1", "admin1", "违规", 7);

        verify(muteMapper).insert(any(ChannelMute.class));
    }

    @Test
    void should_check_mute_status() {
        ChannelMute mute = new ChannelMute();
        mute.setEndTime(new Date(System.currentTimeMillis() + 86400000 * 3));
        when(muteMapper.selectOne(any())).thenReturn(mute);

        assertThat(muteService.isMuted("ch1", "user1")).isTrue();
    }

    @Test
    void should_unmute() {
        ChannelMute mute = new ChannelMute();
        mute.setId("mute1");
        when(muteMapper.selectOne(any())).thenReturn(mute);

        muteService.unmute("ch1", "user1", "admin1");

        assertThat(mute.getUnmuteType()).isEqualTo(2);
        verify(muteMapper).updateById(mute);
    }

    @Test
    void should_reject_unmute_when_no_active_mute() {
        when(muteMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> muteService.unmute("ch1", "user1", "admin1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("未找到有效禁言记录");
    }
}
