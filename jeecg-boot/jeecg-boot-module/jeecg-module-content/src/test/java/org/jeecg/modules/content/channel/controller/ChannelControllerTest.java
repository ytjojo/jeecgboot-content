package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.dto.CreateChannelDTO;
import org.jeecg.modules.content.channel.dto.UpdateChannelDTO;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.enums.TransferStatus;
import org.jeecg.modules.content.channel.req.ChannelListQuery;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.service.ChannelTransferService;
import org.jeecg.modules.content.channel.biz.ChannelBizManageService;
import org.jeecg.modules.content.channel.vo.ChannelTransferVO;
import org.jeecg.modules.content.channel.vo.DeleteCheckResultVO;
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

import java.util.Collections;
import java.util.List;

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
    @Mock
    private ChannelTransferService channelTransferService;

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

    // ===== listMyChannels =====

    @Test
    void should_list_my_channels() {
        ChannelListQuery query = new ChannelListQuery();
        com.baomidou.mybatisplus.core.metadata.IPage<Channel> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);
        when(channelService.listMyChannels(any(), eq("user1"), any())).thenReturn(page);

        Result<?> result = controller.listMyChannels(1, 10, query);

        assertThat(result.isSuccess()).isTrue();
        verify(channelService).listMyChannels(any(), eq("user1"), any());
    }

    // ===== checkDeletePrecondition =====

    @Test
    void should_check_delete_precondition() {
        DeleteCheckResultVO vo = new DeleteCheckResultVO();
        vo.setCanDelete(true);
        when(channelBizManageService.checkDeletePrecondition("ch1", "user1")).thenReturn(vo);

        Result<DeleteCheckResultVO> result = controller.checkDeletePrecondition("ch1");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult().isCanDelete()).isTrue();
    }

    // ===== getTransferHistory =====

    @Test
    void should_get_transfer_history() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setOwnerId("user1");
        when(channelService.getById("ch1")).thenReturn(ch);
        ChannelTransfer transfer = new ChannelTransfer();
        transfer.setId("tr1");
        transfer.setChannelId("ch1");
        transfer.setFromUserId("u1");
        transfer.setToUserId("u2");
        transfer.setStatus(TransferStatus.PENDING);
        when(channelTransferService.getTransferHistory("ch1")).thenReturn(List.of(transfer));

        Result<List<ChannelTransferVO>> result = controller.getTransferHistory("ch1");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).hasSize(1);
        assertThat(result.getResult().get(0).getTransferId()).isEqualTo("tr1");
    }

    // ===== checkNameUnique =====

    @Test
    void should_check_name_unique() {
        when(channelService.checkNameUnique("myname", null)).thenReturn(true);

        Result<Boolean> result = controller.checkNameUnique("myname", null);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isTrue();
    }

    // ===== getPendingTransfer =====

    @Test
    void should_get_pending_transfer() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setOwnerId("user1");
        when(channelService.getById("ch1")).thenReturn(ch);
        ChannelTransfer transfer = new ChannelTransfer();
        transfer.setId("tr1");
        transfer.setChannelId("ch1");
        transfer.setFromUserId("u1");
        transfer.setToUserId("u2");
        transfer.setStatus(TransferStatus.PENDING);
        when(channelTransferService.getPendingTransfer("ch1")).thenReturn(transfer);

        Result<ChannelTransferVO> result = controller.getPendingTransfer("ch1");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult().getTransferId()).isEqualTo("tr1");
    }

    @Test
    void should_return_null_when_no_pending_transfer() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setOwnerId("user1");
        when(channelService.getById("ch1")).thenReturn(ch);
        when(channelTransferService.getPendingTransfer("ch1")).thenReturn(null);

        Result<ChannelTransferVO> result = controller.getPendingTransfer("ch1");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isNull();
    }
}
