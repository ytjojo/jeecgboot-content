package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.entity.ContentCancellationRequest;
import org.jeecg.modules.content.auth.mapper.ContentCancellationRequestMapper;
import org.jeecg.modules.content.auth.service.impl.ContentCancellationRequestServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentCancellationRequestServiceTest {

    @Mock
    private ContentCancellationRequestMapper cancellationRequestMapper;

    @InjectMocks
    private ContentCancellationRequestServiceImpl service;

    @Test
    void shouldInstantiateWithMockMapper() {
        assertThat(service).isNotNull();
    }

    @Test
    void getById_shouldDelegateToMapperAndReturnEntity() {
        Date applyTime = new Date(1717200000000L);
        Date cooldownDeadline = new Date(1717804800000L);
        Date revokeTime = new Date(1717300000000L);
        Date completeTime = new Date(1717804800000L);
        ContentCancellationRequest entity = new ContentCancellationRequest()
            .setUserId("u_4001")
            .setStatus("PENDING")
            .setApplyReason("不再使用该服务")
            .setApplyTime(applyTime)
            .setCooldownDays(7)
            .setCooldownDeadline(cooldownDeadline)
            .setCancelReason("用户主动注销")
            .setRevokeTime(revokeTime)
            .setCompleteTime(completeTime)
            .setOperatorUserId("admin_02")
            .setAnonymized(false);
        when(cancellationRequestMapper.selectById("cancel_req_001")).thenReturn(entity);

        ContentCancellationRequest result = service.getById("cancel_req_001");

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("u_4001");
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getApplyReason()).isEqualTo("不再使用该服务");
        assertThat(result.getApplyTime()).isEqualTo(applyTime);
        assertThat(result.getCooldownDays()).isEqualTo(7);
        assertThat(result.getCooldownDeadline()).isEqualTo(cooldownDeadline);
        assertThat(result.getCancelReason()).isEqualTo("用户主动注销");
        assertThat(result.getRevokeTime()).isEqualTo(revokeTime);
        assertThat(result.getCompleteTime()).isEqualTo(completeTime);
        assertThat(result.getOperatorUserId()).isEqualTo("admin_02");
        assertThat(result.getAnonymized()).isFalse();
        verify(cancellationRequestMapper).selectById("cancel_req_001");
    }

    @Test
    void getById_shouldReturnNullWhenNotFound() {
        when(cancellationRequestMapper.selectById("nonexistent")).thenReturn(null);

        ContentCancellationRequest result = service.getById("nonexistent");

        assertThat(result).isNull();
        verify(cancellationRequestMapper).selectById("nonexistent");
    }
}
