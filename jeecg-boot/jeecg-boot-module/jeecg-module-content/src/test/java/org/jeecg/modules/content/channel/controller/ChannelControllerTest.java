package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.dto.CreateChannelDTO;
import org.jeecg.modules.content.channel.dto.UpdateChannelDTO;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.biz.ChannelBizManageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道主控制器测试
 * 验证用户端频道 CRUD 与转让流程的委托行为
 */
@ExtendWith(MockitoExtension.class)
class ChannelControllerTest {

    @Mock
    private ChannelBizManageService channelBizManageService;
    @Mock
    private ChannelService channelService;

    @InjectMocks
    private ChannelController controller;

    @BeforeEach
    void setUp() {
        LoginUser user = new LoginUser();
        user.setId("user1");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
            JSON.toJSONString(user), null));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_create_personal_channel() {
        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setChannelType(ChannelType.PERSONAL);
        Channel ch = new Channel();
        ch.setId("ch1");
        when(channelBizManageService.createPersonalChannel(dto, "user1")).thenReturn(ch);

        Result<?> result = controller.createChannel(dto);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.isSuccess()).isTrue();
        verify(channelBizManageService).createPersonalChannel(dto, "user1");
    }

    @Test
    void should_create_organization_channel() {
        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setChannelType(ChannelType.ORGANIZATION);
        Channel ch = new Channel();
        ch.setId("ch2");
        when(channelBizManageService.createOrganizationChannel(dto, "user1", true)).thenReturn(ch);

        Result<?> result = controller.createChannel(dto);

        assertThat(result.isSuccess()).isTrue();
        verify(channelBizManageService).createOrganizationChannel(dto, "user1", true);
    }

    @Test
    void should_reject_creating_system_channel_from_user_api() {
        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setChannelType(ChannelType.SYSTEM);

        Result<?> result = controller.createChannel(dto);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("用户端不可创建系统频道");
        verifyNoInteractions(channelBizManageService);
    }

    @Test
    void should_return_channel_detail() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setName("Test");
        when(channelService.getById("ch1")).thenReturn(ch);

        Result<?> result = controller.getChannel("ch1");

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void should_return_error_when_channel_not_found() {
        when(channelService.getById("ch99")).thenReturn(null);

        Result<?> result = controller.getChannel("ch99");

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("频道不存在");
    }

    @Test
    void should_update_channel() {
        UpdateChannelDTO dto = new UpdateChannelDTO();
        dto.setName("NewName");

        Result<Void> result = controller.updateChannel("ch1", dto);

        assertThat(result.isSuccess()).isTrue();
        verify(channelBizManageService).updateChannel("ch1", dto, "user1");
    }

    @Test
    void should_transfer_channel() {
        Result<Void> result = controller.transferChannel("ch1", "user2");

        assertThat(result.isSuccess()).isTrue();
        verify(channelBizManageService).transferChannel("ch1", "user1", "user2");
    }

    @Test
    void should_confirm_transfer() {
        Result<Void> result = controller.confirmTransfer("tr1");

        assertThat(result.isSuccess()).isTrue();
        verify(channelBizManageService).confirmTransfer("tr1", "user1");
    }

    @Test
    void should_reject_transfer() {
        Result<Void> result = controller.rejectTransfer("tr1");

        assertThat(result.isSuccess()).isTrue();
        verify(channelBizManageService).rejectTransfer("tr1", "user1");
    }

    @Test
    void should_delete_channel() {
        Result<Void> result = controller.deleteChannel("ch1");

        assertThat(result.isSuccess()).isTrue();
        verify(channelBizManageService).deleteChannel("ch1", "user1");
    }

    @Test
    void should_cancel_delete() {
        Result<Void> result = controller.cancelDelete("ch1");

        assertThat(result.isSuccess()).isTrue();
        verify(channelBizManageService).cancelDelete("ch1", "user1");
    }
}
