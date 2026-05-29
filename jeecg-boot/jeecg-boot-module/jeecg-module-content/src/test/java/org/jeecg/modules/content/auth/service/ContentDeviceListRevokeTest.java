package org.jeecg.modules.content.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.auth.enums.DeviceSessionStatusEnum;
import org.jeecg.modules.content.auth.service.impl.ContentDeviceSessionServiceImpl;
import org.jeecg.modules.content.auth.vo.DeviceSessionVO;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 设备列表查询与非当前设备下线接口测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentDeviceListRevokeTest {

    @Mock
    private ContentUserDeviceSessionMapper deviceSessionMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private ContentDeviceSessionServiceImpl service;

    private ContentUserDeviceSession session1;
    private ContentUserDeviceSession session2;
    private ContentUserDeviceSession session3;

    @BeforeEach
    void setup() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);

        session1 = new ContentUserDeviceSession();
        session1.setId("sess-1");
        session1.setUserId("user-1");
        session1.setTokenJti("jti-1");
        session1.setDeviceName("iPhone 15");
        session1.setDeviceType("mobile");
        session1.setOsType("iOS");
        session1.setBrowserType("Safari");
        session1.setLoginIp("192.168.1.1");
        session1.setLoginLocation("北京");
        session1.setLastActiveTime(new Date());
        session1.setSessionStatus(DeviceSessionStatusEnum.ACTIVE.getCode());
        session1.setTrusted(true);

        session2 = new ContentUserDeviceSession();
        session2.setId("sess-2");
        session2.setUserId("user-1");
        session2.setTokenJti("jti-2");
        session2.setDeviceName("MacBook Pro");
        session2.setDeviceType("desktop");
        session2.setOsType("macOS");
        session2.setBrowserType("Chrome");
        session2.setLoginIp("192.168.1.2");
        session2.setLoginLocation("上海");
        session2.setLastActiveTime(new Date());
        session2.setSessionStatus(DeviceSessionStatusEnum.ACTIVE.getCode());
        session2.setTrusted(false);

        session3 = new ContentUserDeviceSession();
        session3.setId("sess-3");
        session3.setUserId("user-1");
        session3.setTokenJti("jti-3");
        session3.setDeviceName("iPad");
        session3.setDeviceType("tablet");
        session3.setOsType("iPadOS");
        session3.setBrowserType("Safari");
        session3.setLoginIp("192.168.1.3");
        session3.setLoginLocation("广州");
        session3.setLastActiveTime(new Date());
        session3.setSessionStatus(DeviceSessionStatusEnum.ACTIVE.getCode());
        session3.setTrusted(false);
    }

    @Test
    void listDevices_shouldReturnAllActiveSessionsWithCorrectFields() {
        when(deviceSessionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(session1, session2));

        List<DeviceSessionVO> result = service.listDevices("user-1", "jti-1");

        assertThat(result).hasSize(2);

        DeviceSessionVO vo1 = result.get(0);
        assertThat(vo1.getSessionId()).isEqualTo("sess-1");
        assertThat(vo1.getDeviceName()).isEqualTo("iPhone 15");
        assertThat(vo1.getDeviceType()).isEqualTo("mobile");
        assertThat(vo1.getOsType()).isEqualTo("iOS");
        assertThat(vo1.getBrowserType()).isEqualTo("Safari");
        assertThat(vo1.getLoginIp()).isEqualTo("192.168.1.1");
        assertThat(vo1.getLoginLocation()).isEqualTo("北京");
        assertThat(vo1.isCurrent()).isTrue();
        assertThat(vo1.isTrusted()).isTrue();

        DeviceSessionVO vo2 = result.get(1);
        assertThat(vo2.getSessionId()).isEqualTo("sess-2");
        assertThat(vo2.isCurrent()).isFalse();
        assertThat(vo2.isTrusted()).isFalse();
    }

    @Test
    void listDevices_shouldMarkCurrentDeviceCorrectly() {
        when(deviceSessionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(session1, session2, session3));

        List<DeviceSessionVO> result = service.listDevices("user-1", "jti-2");

        assertThat(result).hasSize(3);

        // session1 (jti-1) should not be current
        DeviceSessionVO vo1 = result.stream().filter(v -> v.getSessionId().equals("sess-1")).findFirst().orElseThrow();
        assertThat(vo1.isCurrent()).isFalse();

        // session2 (jti-2) should be current
        DeviceSessionVO vo2 = result.stream().filter(v -> v.getSessionId().equals("sess-2")).findFirst().orElseThrow();
        assertThat(vo2.isCurrent()).isTrue();

        // session3 (jti-3) should not be current
        DeviceSessionVO vo3 = result.stream().filter(v -> v.getSessionId().equals("sess-3")).findFirst().orElseThrow();
        assertThat(vo3.isCurrent()).isFalse();
    }

    @Test
    void listDevices_shouldReturnEmptyListWhenNoSessions() {
        when(deviceSessionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<DeviceSessionVO> result = service.listDevices("user-1", "jti-1");

        assertThat(result).isEmpty();
    }

    @Test
    void revokeOtherDevice_shouldSuccessfullyRevokeNonCurrentDevice() {
        when(deviceSessionMapper.selectById("sess-2")).thenReturn(session2);

        service.revokeOtherDevice("user-1", "sess-2", "jti-1");

        ArgumentCaptor<ContentUserDeviceSession> captor = ArgumentCaptor.forClass(ContentUserDeviceSession.class);
        verify(deviceSessionMapper).updateById(captor.capture());
        ContentUserDeviceSession updated = captor.getValue();
        assertThat(updated.getSessionStatus()).isEqualTo(DeviceSessionStatusEnum.OFFLINE.getCode());
        assertThat(updated.getOfflineReason()).isEqualTo("USER_REVOKED");
        assertThat(updated.getOfflineTime()).isNotNull();
    }

    @Test
    void revokeOtherDevice_shouldThrowWhenTryingToRevokeCurrentDevice() {
        when(deviceSessionMapper.selectById("sess-1")).thenReturn(session1);

        assertThatThrownBy(() -> service.revokeOtherDevice("user-1", "sess-1", "jti-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("不能下线当前设备");

        verify(deviceSessionMapper, never()).updateById(any(ContentUserDeviceSession.class));
    }

    @Test
    void revokeOtherDevice_shouldWriteJtiToRedisBlacklist() {
        when(deviceSessionMapper.selectById("sess-2")).thenReturn(session2);

        service.revokeOtherDevice("user-1", "sess-2", "jti-1");

        verify(valueOps).set(
                eq("content:auth:token_blacklist:jti-2"),
                eq("REVOKED"),
                eq(Duration.ofHours(24))
        );
    }

    @Test
    void revokeOtherDevice_shouldThrowForNonExistentSession() {
        when(deviceSessionMapper.selectById("non-existent")).thenReturn(null);

        assertThatThrownBy(() -> service.revokeOtherDevice("user-1", "non-existent", "jti-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("会话不存在");
    }

    @Test
    void revokeOtherDevice_shouldThrowForSessionBelongingToDifferentUser() {
        ContentUserDeviceSession otherUserSession = new ContentUserDeviceSession();
        otherUserSession.setId("sess-other");
        otherUserSession.setUserId("other-user");
        otherUserSession.setTokenJti("jti-other");
        otherUserSession.setSessionStatus(DeviceSessionStatusEnum.ACTIVE.getCode());

        when(deviceSessionMapper.selectById("sess-other")).thenReturn(otherUserSession);

        assertThatThrownBy(() -> service.revokeOtherDevice("user-1", "sess-other", "jti-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("会话不属于该用户");

        verify(deviceSessionMapper, never()).updateById(any(ContentUserDeviceSession.class));
    }
}
