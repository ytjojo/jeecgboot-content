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

/**
 * 隐私可见性集成测试
 * 验证频道隐私设置的业务规则：系统频道必须公开，私有频道需权限控制
 */
@ExtendWith(MockitoExtension.class)
class ChannelPrivacyTest {

    @Mock
    private ChannelService channelService;

    @InjectMocks
    private ChannelPrivacyServiceImpl privacyService;

    @Test
    void should_set_personal_channel_to_private() {
        // 个人频道可以设置为私有，这是隐私控制的核心功能
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
        // 系统频道必须保持公开，这是平台级频道的硬性约束
        Channel channel = new Channel();
        channel.setId("sys1");
        channel.setChannelType(ChannelType.SYSTEM);
        when(channelService.getById("sys1")).thenReturn(channel);

        assertThatThrownBy(() -> privacyService.updatePrivacy("sys1", PrivacyType.PRIVATE, "admin1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("系统频道必须公开");
    }

    @Test
    void should_throw_when_channel_not_found() {
        // 操作不存在的频道应明确报错，避免静默失败
        when(channelService.getById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> privacyService.updatePrivacy("nonexistent", PrivacyType.PRIVATE, "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("频道不存在");
    }

    @Test
    void should_allow_setting_system_channel_to_public() {
        // 系统频道可以设为公开（已经是公开的也应无异常）
        Channel channel = new Channel();
        channel.setId("sys1");
        channel.setChannelType(ChannelType.SYSTEM);
        channel.setPrivacy(PrivacyType.PUBLIC.getCode());
        when(channelService.getById("sys1")).thenReturn(channel);

        assertThatCode(() -> privacyService.updatePrivacy("sys1", PrivacyType.PUBLIC, "admin1"))
            .doesNotThrowAnyException();
    }

    @Test
    void should_allow_setting_organization_channel_to_private() {
        // 组织频道也可以设为私有，与系统频道的限制形成对比
        Channel channel = new Channel();
        channel.setId("org1");
        channel.setChannelType(ChannelType.ORGANIZATION);
        channel.setPrivacy(PrivacyType.PUBLIC.getCode());
        when(channelService.getById("org1")).thenReturn(channel);

        privacyService.updatePrivacy("org1", PrivacyType.PRIVATE, "admin1");

        assertThat(channel.getPrivacy()).isEqualTo(PrivacyType.PRIVATE.getCode());
        verify(channelService).updateById(channel);
    }
}
