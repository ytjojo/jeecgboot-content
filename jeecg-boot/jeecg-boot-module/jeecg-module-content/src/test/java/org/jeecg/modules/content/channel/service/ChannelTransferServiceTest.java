package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;
import org.jeecg.modules.content.channel.enums.TransferStatus;
import org.jeecg.modules.content.channel.mapper.ChannelTransferMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelTransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 频道转让服务测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelTransferServiceTest {

    @Mock
    private ChannelTransferMapper transferMapper;

    @InjectMocks
    private ChannelTransferServiceImpl transferService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transferService, "baseMapper", transferMapper);
    }

    @Test
    void should_create_pending_transfer() {
        ChannelTransfer result = transferService.createTransfer("ch1", "u1", "u2");

        assertThat(result.getStatus()).isEqualTo(TransferStatus.PENDING);
        assertThat(result.getChannelId()).isEqualTo("ch1");
        assertThat(result.getFromUserId()).isEqualTo("u1");
        assertThat(result.getToUserId()).isEqualTo("u2");
        assertThat(result.getExpireTime()).isAfter(new Date());
        verify(transferMapper).insert(result);
    }

    @Test
    void should_return_null_when_confirm_target_not_exist() {
        when(transferMapper.selectById("nope")).thenReturn(null);

        assertThat(transferService.confirmTransfer("nope", "u2")).isNull();
    }

    @Test
    void should_return_null_when_target_user_mismatch() {
        ChannelTransfer transfer = new ChannelTransfer();
        transfer.setId("tr1");
        transfer.setToUserId("u3");
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setExpireTime(new Date(System.currentTimeMillis() + 60_000));
        when(transferMapper.selectById("tr1")).thenReturn(transfer);

        assertThat(transferService.confirmTransfer("tr1", "u2")).isNull();
    }

    @Test
    void should_mark_expired_and_return_null_when_overdue() {
        ChannelTransfer transfer = new ChannelTransfer();
        transfer.setId("tr1");
        transfer.setToUserId("u2");
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setExpireTime(new Date(System.currentTimeMillis() - 60_000));
        when(transferMapper.selectById("tr1")).thenReturn(transfer);

        assertThat(transferService.confirmTransfer("tr1", "u2")).isNull();
        verify(transferMapper).updateById(transfer);
        assertThat(transfer.getStatus()).isEqualTo(TransferStatus.EXPIRED);
    }

    @Test
    void should_accept_transfer_on_confirm() {
        ChannelTransfer transfer = new ChannelTransfer();
        transfer.setId("tr1");
        transfer.setToUserId("u2");
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setExpireTime(new Date(System.currentTimeMillis() + 60_000));
        when(transferMapper.selectById("tr1")).thenReturn(transfer);

        ChannelTransfer confirmed = transferService.confirmTransfer("tr1", "u2");

        assertThat(confirmed).isNotNull();
        assertThat(confirmed.getStatus()).isEqualTo(TransferStatus.ACCEPTED);
        verify(transferMapper).updateById(transfer);
    }

    @Test
    void should_reject_transfer_for_non_pending() {
        ChannelTransfer transfer = new ChannelTransfer();
        transfer.setId("tr1");
        transfer.setToUserId("u2");
        transfer.setStatus(TransferStatus.ACCEPTED);
        when(transferMapper.selectById("tr1")).thenReturn(transfer);

        assertThat(transferService.rejectTransfer("tr1", "u2")).isFalse();
    }

    @Test
    void should_reject_transfer_for_wrong_user() {
        ChannelTransfer transfer = new ChannelTransfer();
        transfer.setId("tr1");
        transfer.setToUserId("u3");
        transfer.setStatus(TransferStatus.PENDING);
        when(transferMapper.selectById("tr1")).thenReturn(transfer);

        assertThat(transferService.rejectTransfer("tr1", "u2")).isFalse();
    }

    @Test
    void should_mark_rejected_on_reject() {
        ChannelTransfer transfer = new ChannelTransfer();
        transfer.setId("tr1");
        transfer.setToUserId("u2");
        transfer.setStatus(TransferStatus.PENDING);
        when(transferMapper.selectById("tr1")).thenReturn(transfer);

        assertThat(transferService.rejectTransfer("tr1", "u2")).isTrue();
        assertThat(transfer.getStatus()).isEqualTo(TransferStatus.REJECTED);
    }

    // ===== getTransferHistory =====

    @Test
    void should_return_transfer_history_ordered_by_create_time() {
        ChannelTransfer t1 = new ChannelTransfer();
        t1.setId("tr1");
        t1.setChannelId("ch1");
        ChannelTransfer t2 = new ChannelTransfer();
        t2.setId("tr2");
        t2.setChannelId("ch1");
        when(transferMapper.selectList(any())).thenReturn(List.of(t2, t1));

        List<ChannelTransfer> result = transferService.getTransferHistory("ch1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("tr2");
    }

    @Test
    void should_return_empty_list_when_no_transfer_history() {
        when(transferMapper.selectList(any())).thenReturn(List.of());

        List<ChannelTransfer> result = transferService.getTransferHistory("ch1");

        assertThat(result).isEmpty();
    }

    // ===== getPendingTransfer =====

    @Test
    void should_return_pending_transfer() {
        ChannelTransfer transfer = new ChannelTransfer();
        transfer.setId("tr1");
        transfer.setChannelId("ch1");
        transfer.setStatus(TransferStatus.PENDING);
        when(transferMapper.selectOne(any())).thenReturn(transfer);

        ChannelTransfer result = transferService.getPendingTransfer("ch1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("tr1");
    }

    @Test
    void should_return_null_when_no_pending_transfer() {
        when(transferMapper.selectOne(any())).thenReturn(null);

        ChannelTransfer result = transferService.getPendingTransfer("ch1");

        assertThat(result).isNull();
    }
}
