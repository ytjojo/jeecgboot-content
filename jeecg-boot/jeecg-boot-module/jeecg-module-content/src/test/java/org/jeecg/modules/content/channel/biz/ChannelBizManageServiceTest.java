package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.constant.ChannelConstants;
import org.jeecg.modules.content.channel.dto.CreateChannelDTO;
import org.jeecg.modules.content.channel.dto.UpdateChannelDTO;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.enums.ReviewResult;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.service.ChannelTransferService;
import org.jeecg.modules.content.channel.service.IChannelReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 频道核心业务编排测试
 * 覆盖创建/更新/转让/审核/删除等跨表事务
 */
@ExtendWith(MockitoExtension.class)
class ChannelBizManageServiceTest {

    @Mock
    private ChannelService channelService;
    @Mock
    private IChannelReviewService channelReviewService;
    @Mock
    private ChannelTransferService channelTransferService;

    @InjectMocks
    private ChannelBizManageService bizService;

    // ===== createSystemChannel =====

    @Test
    void should_create_system_channel_with_operator_as_owner() {
        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setName("sys-channel");
        dto.setPinWeight(50);

        Channel ch = bizService.createSystemChannel(dto, "admin1");

        assertThat(ch.getOwnerId()).isEqualTo("admin1");
        assertThat(ch.getChannelType()).isEqualTo(ChannelType.SYSTEM);
        assertThat(ch.getStatus()).isEqualTo(ChannelStatus.ACTIVE);
        assertThat(ch.getPinWeight()).isEqualTo(50);
        verify(channelService).save(ch);
    }

    @Test
    void should_default_pin_weight_to_zero_when_null() {
        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setName("sys");
        dto.setPinWeight(null);

        Channel ch = bizService.createSystemChannel(dto, "admin1");

        assertThat(ch.getPinWeight()).isEqualTo(0);
    }

    // ===== createPersonalChannel =====

    @Test
    void should_reject_duplicate_personal_channel_name() {
        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setName("dup");
        when(channelService.checkNameUnique("dup", null)).thenReturn(false);

        assertThatThrownBy(() -> bizService.createPersonalChannel(dto, "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("该频道名称已被使用");
    }

    @Test
    void should_reject_personal_channel_over_quota() {
        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setName("new");
        when(channelService.checkNameUnique("new", null)).thenReturn(true);
        when(channelService.count(any(LambdaQueryWrapper.class)))
            .thenReturn((long) ChannelConstants.MAX_PERSONAL_CHANNELS);

        assertThatThrownBy(() -> bizService.createPersonalChannel(dto, "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("个人频道数量已达上限");
    }

    @Test
    void should_create_personal_channel_in_pending_review() {
        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setName("personal");
        when(channelService.checkNameUnique("personal", null)).thenReturn(true);
        when(channelService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);

        Channel ch = bizService.createPersonalChannel(dto, "user1");

        assertThat(ch.getStatus()).isEqualTo(ChannelStatus.PENDING_REVIEW);
        assertThat(ch.getOwnerId()).isEqualTo("user1");
        verify(channelService).save(ch);
    }

    // ===== createOrganizationChannel =====

    @Test
    void should_reject_org_channel_when_not_certified() {
        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setName("org");

        assertThatThrownBy(() -> bizService.createOrganizationChannel(dto, "user1", false))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("请先完成组织认证");
    }

    @Test
    void should_reject_org_channel_over_quota() {
        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setName("org");
        dto.setOrganizationId("org1");
        when(channelService.checkNameUnique("org", null)).thenReturn(true);
        when(channelService.count(any(LambdaQueryWrapper.class)))
            .thenReturn((long) ChannelConstants.MAX_ORG_CHANNELS);

        assertThatThrownBy(() -> bizService.createOrganizationChannel(dto, "user1", true))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("组织频道数量已达上限");
    }

    @Test
    void should_create_organization_channel_in_pending_review() {
        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setName("org");
        dto.setOrganizationId("org1");
        when(channelService.checkNameUnique("org", null)).thenReturn(true);
        when(channelService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);

        Channel ch = bizService.createOrganizationChannel(dto, "user1", true);

        assertThat(ch.getChannelType()).isEqualTo(ChannelType.ORGANIZATION);
        assertThat(ch.getOrganizationId()).isEqualTo("org1");
        verify(channelService).save(ch);
    }

    // ===== updateChannel =====

    @Test
    void should_error_when_update_target_not_exist() {
        when(channelService.getById("nope")).thenReturn(null);
        UpdateChannelDTO dto = new UpdateChannelDTO();
        assertThatThrownBy(() -> bizService.updateChannel("nope", dto, "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("频道不存在");
    }

    @Test
    void should_reject_renamed_duplicate_name_on_update() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setName("old");
        ch.setChannelType(ChannelType.PERSONAL);
        when(channelService.getById("ch1")).thenReturn(ch);
        when(channelService.checkNameUnique("new", "ch1")).thenReturn(false);

        UpdateChannelDTO dto = new UpdateChannelDTO();
        dto.setName("new");

        assertThatThrownBy(() -> bizService.updateChannel("ch1", dto, "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("该频道名称已被使用");
    }

    @Test
    void should_update_basic_fields_without_triggering_review() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setName("name");
        ch.setChannelType(ChannelType.SYSTEM);
        when(channelService.getById("ch1")).thenReturn(ch);

        UpdateChannelDTO dto = new UpdateChannelDTO();
        dto.setDescription("new-desc");
        dto.setIconUrl("icon");

        bizService.updateChannel("ch1", dto, "user1");

        assertThat(ch.getDescription()).isEqualTo("new-desc");
        assertThat(ch.getIconUrl()).isEqualTo("icon");
        verify(channelService).updateById(ch);
        verifyNoInteractions(channelReviewService);
    }

    @Test
    void should_trigger_review_when_personal_channel_name_changed() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setName("old");
        ch.setChannelType(ChannelType.PERSONAL);
        ch.setStatus(ChannelStatus.ACTIVE);
        when(channelService.getById("ch1")).thenReturn(ch);
        when(channelService.checkNameUnique("new", "ch1")).thenReturn(true);

        UpdateChannelDTO dto = new UpdateChannelDTO();
        dto.setName("new");

        bizService.updateChannel("ch1", dto, "user1");

        assertThat(ch.getStatus()).isEqualTo(ChannelStatus.PENDING_REVIEW);
        verify(channelReviewService).submitReview("ch1", "update_field", "user1", "关键字段修改触发审核");
    }

    // ===== transferChannel =====

    @Test
    void should_reject_transfer_to_nonexistent_channel() {
        when(channelService.getById("nope")).thenReturn(null);
        assertThatThrownBy(() -> bizService.transferChannel("nope", "u1", "u2"))
            .isInstanceOf(JeecgBootException.class);
    }

    @Test
    void should_reject_transfer_of_system_channel() {
        Channel ch = new Channel();
        ch.setId("sys1");
        ch.setChannelType(ChannelType.SYSTEM);
        when(channelService.getById("sys1")).thenReturn(ch);

        assertThatThrownBy(() -> bizService.transferChannel("sys1", "u1", "u2"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("系统频道不可转让");
    }

    @Test
    void should_reject_transfer_by_non_owner() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setChannelType(ChannelType.PERSONAL);
        ch.setOwnerId("owner1");
        when(channelService.getById("ch1")).thenReturn(ch);

        assertThatThrownBy(() -> bizService.transferChannel("ch1", "stranger", "u2"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("仅频道主可发起转让");
    }

    @Test
    void should_reject_organization_channel_transfer_without_org_binding() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setChannelType(ChannelType.ORGANIZATION);
        ch.setOwnerId("u1");
        ch.setOrganizationId(null);
        when(channelService.getById("ch1")).thenReturn(ch);

        assertThatThrownBy(() -> bizService.transferChannel("ch1", "u1", "u2"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("未绑定组织");
    }

    @Test
    void should_create_transfer_request() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setChannelType(ChannelType.PERSONAL);
        ch.setOwnerId("u1");
        when(channelService.getById("ch1")).thenReturn(ch);

        bizService.transferChannel("ch1", "u1", "u2");

        verify(channelTransferService).createTransfer("ch1", "u1", "u2");
    }

    // ===== confirmTransfer =====

    @Test
    void should_error_when_confirm_transfer_returns_null() {
        when(channelTransferService.confirmTransfer("tr1", "u1")).thenReturn(null);
        assertThatThrownBy(() -> bizService.confirmTransfer("tr1", "u1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("转让确认失败");
    }

    @Test
    void should_change_owner_on_confirm() {
        ChannelTransfer transfer = new ChannelTransfer();
        transfer.setChannelId("ch1");
        transfer.setToUserId("u2");
        when(channelTransferService.confirmTransfer("tr1", "u1")).thenReturn(transfer);
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setOwnerId("u1");
        when(channelService.getById("ch1")).thenReturn(ch);

        bizService.confirmTransfer("tr1", "u1");

        assertThat(ch.getOwnerId()).isEqualTo("u2");
        verify(channelService).updateById(ch);
    }

    // ===== rejectTransfer =====

    @Test
    void should_error_when_reject_transfer_fails() {
        when(channelTransferService.rejectTransfer("tr1", "u1")).thenReturn(false);
        assertThatThrownBy(() -> bizService.rejectTransfer("tr1", "u1"))
            .isInstanceOf(JeecgBootException.class);
    }

    @Test
    void should_pass_through_reject_transfer() {
        when(channelTransferService.rejectTransfer("tr1", "u1")).thenReturn(true);
        bizService.rejectTransfer("tr1", "u1");
        verify(channelTransferService).rejectTransfer("tr1", "u1");
    }

    // ===== deleteChannel =====

    @Test
    void should_reject_deleting_system_channel() {
        Channel ch = new Channel();
        ch.setId("sys1");
        ch.setChannelType(ChannelType.SYSTEM);
        when(channelService.getById("sys1")).thenReturn(ch);

        assertThatThrownBy(() -> bizService.deleteChannel("sys1", "u1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("系统频道仅平台可管理");
    }

    @Test
    void should_enter_cooling_period_on_delete() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setChannelType(ChannelType.PERSONAL);
        ch.setOwnerId("u1");
        when(channelService.getById("ch1")).thenReturn(ch);

        bizService.deleteChannel("ch1", "u1");

        ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);
        verify(channelService).updateById(captor.capture());
        Channel saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(ChannelStatus.DELETE_COOLING);
        assertThat(saved.getDeleteCoolingEndTime()).isNotNull();
        assertThat(saved.getDeleteCoolingEndTime()).isAfter(new Date());
    }

    // ===== cancelDelete =====

    @Test
    void should_reject_cancel_when_not_in_cooling() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setStatus(ChannelStatus.ACTIVE);
        when(channelService.getById("ch1")).thenReturn(ch);

        assertThatThrownBy(() -> bizService.cancelDelete("ch1", "u1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("频道不在冷静期内");
    }

    @Test
    void should_reject_cancel_when_cooling_expired() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setStatus(ChannelStatus.DELETE_COOLING);
        ch.setOwnerId("u1");
        ch.setDeleteCoolingEndTime(new Date(System.currentTimeMillis() - 60_000));
        when(channelService.getById("ch1")).thenReturn(ch);

        assertThatThrownBy(() -> bizService.cancelDelete("ch1", "u1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("冷静期已过");
    }

    @Test
    void should_restore_channel_on_cancel() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setStatus(ChannelStatus.DELETE_COOLING);
        ch.setOwnerId("u1");
        ch.setDeleteCoolingEndTime(new Date(System.currentTimeMillis() + 60_000));
        when(channelService.getById("ch1")).thenReturn(ch);

        bizService.cancelDelete("ch1", "u1");

        assertThat(ch.getStatus()).isEqualTo(ChannelStatus.ACTIVE);
        assertThat(ch.getDeleteCoolingEndTime()).isNull();
        verify(channelService).updateById(ch);
    }

    // ===== reviewChannel =====

    @Test
    void should_reject_review_when_channel_not_pending() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setStatus(ChannelStatus.ACTIVE);
        when(channelService.getById("ch1")).thenReturn(ch);

        assertThatThrownBy(() -> bizService.reviewChannel("ch1", "rv1", ReviewResult.PASS, null))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("频道当前状态不可审核");
    }

    @Test
    void should_activate_channel_on_pass() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setStatus(ChannelStatus.PENDING_REVIEW);
        when(channelService.getById("ch1")).thenReturn(ch);

        bizService.reviewChannel("ch1", "rv1", ReviewResult.PASS, null);

        assertThat(ch.getStatus()).isEqualTo(ChannelStatus.ACTIVE);
        verify(channelReviewService).createReview("ch1", "rv1", ReviewResult.PASS, null);
    }

    @Test
    void should_reject_channel_on_review_reject() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setStatus(ChannelStatus.PENDING_REVIEW);
        when(channelService.getById("ch1")).thenReturn(ch);

        bizService.reviewChannel("ch1", "rv1", ReviewResult.REJECT, "bad");

        assertThat(ch.getStatus()).isEqualTo(ChannelStatus.REJECTED);
    }

    @Test
    void should_return_to_draft_on_return_for_edit() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setStatus(ChannelStatus.PENDING_REVIEW);
        when(channelService.getById("ch1")).thenReturn(ch);

        bizService.reviewChannel("ch1", "rv1", ReviewResult.RETURN_FOR_EDIT, "fix");

        assertThat(ch.getStatus()).isEqualTo(ChannelStatus.DRAFT);
    }
}
