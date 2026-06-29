package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.entity.ContentRiskEvent;
import org.jeecg.modules.content.auth.mapper.ContentRiskEventMapper;
import org.jeecg.modules.content.auth.service.impl.ContentRiskEventServiceImpl;
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
class ContentRiskEventServiceTest {

    @Mock
    private ContentRiskEventMapper riskEventMapper;

    @InjectMocks
    private ContentRiskEventServiceImpl service;

    @Test
    void shouldInstantiateWithMockMapper() {
        assertThat(service).isNotNull();
    }

    @Test
    void getById_shouldDelegateToMapperAndReturnEntity() {
        Date resolvedAt = new Date(1717200000000L);
        ContentRiskEvent entity = new ContentRiskEvent()
            .setUserId("u_3001")
            .setEventType("ABNORMAL_LOGIN")
            .setRiskLevel(3)
            .setRiskScore(85)
            .setRiskReason("异地IP登录")
            .setDecision("CHALLENGE")
            .setIpAddress("203.0.113.7")
            .setDeviceFingerprint("fp_xyz_001")
            .setUserAgent("Mozilla/5.0")
            .setExtraDataJson("{\"k\":\"v\"}")
            .setResolved(true)
            .setResolvedBy("admin_01")
            .setResolvedAt(resolvedAt)
            .setResolveNote("已确认本人操作");
        when(riskEventMapper.selectById("risk_evt_001")).thenReturn(entity);

        ContentRiskEvent result = service.getById("risk_evt_001");

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("u_3001");
        assertThat(result.getEventType()).isEqualTo("ABNORMAL_LOGIN");
        assertThat(result.getRiskLevel()).isEqualTo(3);
        assertThat(result.getRiskScore()).isEqualTo(85);
        assertThat(result.getRiskReason()).isEqualTo("异地IP登录");
        assertThat(result.getDecision()).isEqualTo("CHALLENGE");
        assertThat(result.getIpAddress()).isEqualTo("203.0.113.7");
        assertThat(result.getDeviceFingerprint()).isEqualTo("fp_xyz_001");
        assertThat(result.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(result.getExtraDataJson()).isEqualTo("{\"k\":\"v\"}");
        assertThat(result.getResolved()).isTrue();
        assertThat(result.getResolvedBy()).isEqualTo("admin_01");
        assertThat(result.getResolvedAt()).isEqualTo(resolvedAt);
        assertThat(result.getResolveNote()).isEqualTo("已确认本人操作");
        verify(riskEventMapper).selectById("risk_evt_001");
    }

    @Test
    void getById_shouldReturnNullWhenNotFound() {
        when(riskEventMapper.selectById("nonexistent")).thenReturn(null);

        ContentRiskEvent result = service.getById("nonexistent");

        assertThat(result).isNull();
        verify(riskEventMapper).selectById("nonexistent");
    }
}
