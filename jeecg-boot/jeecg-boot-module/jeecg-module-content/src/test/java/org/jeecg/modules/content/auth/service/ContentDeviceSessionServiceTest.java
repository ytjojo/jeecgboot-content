package org.jeecg.modules.content.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.auth.dto.DeviceInfo;
import org.jeecg.modules.content.auth.enums.DeviceSessionStatusEnum;
import org.jeecg.modules.content.auth.service.impl.ContentDeviceSessionServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 设备会话服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentDeviceSessionServiceTest {

    @Mock
    private ContentUserDeviceSessionMapper deviceSessionMapper;

    @InjectMocks
    private ContentDeviceSessionServiceImpl service;

    private DeviceInfo buildDeviceInfo() {
        return new DeviceInfo()
                .setDeviceId("dev-001")
                .setDeviceName("iPhone 15")
                .setDeviceType("MOBILE")
                .setOsType("iOS")
                .setOsVersion("17.0")
                .setBrowserType("Safari")
                .setDeviceFingerprint("fp-abc123")
                .setLoginIp("192.168.1.1")
                .setLoginLocation("Shanghai");
    }

    @Test
    void createSession_shouldReturnSessionWithAllFieldsPopulated() {
        DeviceInfo deviceInfo = buildDeviceInfo();
        when(deviceSessionMapper.insert(any(ContentUserDeviceSession.class))).thenReturn(1);

        ContentUserDeviceSession result = service.createSession("user-001", "jti-abc", deviceInfo);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user-001");
        assertThat(result.getTokenJti()).isEqualTo("jti-abc");
        assertThat(result.getDeviceId()).isEqualTo("dev-001");
        assertThat(result.getDeviceName()).isEqualTo("iPhone 15");
        assertThat(result.getDeviceType()).isEqualTo("MOBILE");
        assertThat(result.getOsType()).isEqualTo("iOS");
        assertThat(result.getOsVersion()).isEqualTo("17.0");
        assertThat(result.getBrowserType()).isEqualTo("Safari");
        assertThat(result.getDeviceFingerprint()).isEqualTo("fp-abc123");
        assertThat(result.getLoginIp()).isEqualTo("192.168.1.1");
        assertThat(result.getLoginLocation()).isEqualTo("Shanghai");
    }

    @Test
    void createSession_shouldSetSessionStatusToActive() {
        DeviceInfo deviceInfo = buildDeviceInfo();
        when(deviceSessionMapper.insert(any(ContentUserDeviceSession.class))).thenReturn(1);

        ContentUserDeviceSession result = service.createSession("user-001", "jti-abc", deviceInfo);

        assertThat(result.getSessionStatus()).isEqualTo(DeviceSessionStatusEnum.ACTIVE.getCode());
        assertThat(result.getLastActiveTime()).isNotNull();
    }

    @Test
    void listActiveSessions_shouldReturnOnlyActiveSessions() {
        ContentUserDeviceSession activeSession = new ContentUserDeviceSession();
        activeSession.setId("s1");
        activeSession.setSessionStatus(DeviceSessionStatusEnum.ACTIVE.getCode());
        when(deviceSessionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(activeSession));

        List<ContentUserDeviceSession> result = service.listActiveSessions("user-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSessionStatus()).isEqualTo(DeviceSessionStatusEnum.ACTIVE.getCode());
    }

    @Test
    void listActiveSessions_shouldReturnEmptyListWhenNoSessions() {
        when(deviceSessionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<ContentUserDeviceSession> result = service.listActiveSessions("user-001");

        assertThat(result).isEmpty();
    }

    @Test
    void revokeSession_shouldSetStatusToOfflineWithOfflineTime() {
        ContentUserDeviceSession existing = new ContentUserDeviceSession();
        existing.setId("s1");
        existing.setUserId("user-001");
        existing.setSessionStatus(DeviceSessionStatusEnum.ACTIVE.getCode());
        when(deviceSessionMapper.selectById("s1")).thenReturn(existing);
        when(deviceSessionMapper.updateById(any(ContentUserDeviceSession.class))).thenReturn(1);

        service.revokeSession("s1", "user-001");

        ArgumentCaptor<ContentUserDeviceSession> captor = ArgumentCaptor.forClass(ContentUserDeviceSession.class);
        verify(deviceSessionMapper).updateById(captor.capture());
        ContentUserDeviceSession updated = captor.getValue();
        assertThat(updated.getSessionStatus()).isEqualTo(DeviceSessionStatusEnum.OFFLINE.getCode());
        assertThat(updated.getOfflineTime()).isNotNull();
        assertThat(updated.getOfflineReason()).isEqualTo("USER_REVOKED");
    }

    @Test
    void revokeSession_withNonExistentSession_shouldThrowException() {
        when(deviceSessionMapper.selectById("non-existent")).thenReturn(null);

        assertThatThrownBy(() -> service.revokeSession("non-existent", "user-001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("会话不存在");
    }

    @Test
    void isTokenValid_shouldReturnTrueForActiveSession() {
        ContentUserDeviceSession activeSession = new ContentUserDeviceSession();
        activeSession.setSessionStatus(DeviceSessionStatusEnum.ACTIVE.getCode());
        when(deviceSessionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(activeSession);

        boolean result = service.isTokenValid("jti-abc");

        assertThat(result).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseWhenQueryReturnsNull() {
        // 当token jti对应的会话不存在或非ACTIVE状态时，selectOne返回null
        when(deviceSessionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        boolean result = service.isTokenValid("jti-abc");

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalseForNonExistentJti() {
        when(deviceSessionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        boolean result = service.isTokenValid("non-existent-jti");

        assertThat(result).isFalse();
    }
}
