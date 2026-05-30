package org.jeecg.modules.content.channel.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.enums.PrivacyType;
import org.jeecg.modules.content.channel.service.impl.ChannelPrivacyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelPrivacyServiceTest {

    @Mock
    private ChannelService channelService;

    @InjectMocks
    private ChannelPrivacyServiceImpl privacyService;

    @Test
    void should_set_channel_to_private() {
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setChannelType(ChannelType.PERSONAL);
        channel.setPrivacy(PrivacyType.PUBLIC.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);

        privacyService.updatePrivacy("ch1", PrivacyType.PRIVATE, "user1");

        assertThat(channel.getPrivacy()).isEqualTo(PrivacyType.PRIVATE.getCode());
        verify(channelService).updateById(channel);
    }

    @Test
    void should_reject_setting_system_channel_to_private() {
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setChannelType(ChannelType.SYSTEM);
        when(channelService.getById("ch1")).thenReturn(channel);

        assertThatThrownBy(() -> privacyService.updatePrivacy("ch1", PrivacyType.PRIVATE, "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("系统频道必须公开");
    }

    @Test
    void should_reject_when_channel_not_found() {
        when(channelService.getById("ch999")).thenReturn(null);

        assertThatThrownBy(() -> privacyService.updatePrivacy("ch999", PrivacyType.PRIVATE, "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("频道不存在");
    }

    @Test
    void should_allow_setting_system_channel_to_public() {
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setChannelType(ChannelType.SYSTEM);
        channel.setPrivacy(PrivacyType.PUBLIC.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);

        assertThatCode(() -> privacyService.updatePrivacy("ch1", PrivacyType.PUBLIC, "user1"))
            .doesNotThrowAnyException();
    }
}
