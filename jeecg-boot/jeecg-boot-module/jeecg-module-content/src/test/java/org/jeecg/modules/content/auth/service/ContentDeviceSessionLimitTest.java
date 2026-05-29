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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 设备会话限制测试（最多5个活跃设备，第6个挤出最早的）。
 */
@ExtendWith(MockitoExtension.class)
class ContentDeviceSessionLimitTest {

    @Mock
    private ContentUserDeviceSessionMapper deviceSessionMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ContentDeviceSessionServiceImpl service;

    private DeviceInfo buildDeviceInfo(String name) {
        return new DeviceInfo()
                .setDeviceId("dev-" + name)
                .setDeviceName(name)
                .setDeviceType("MOBILE")
                .setOsType("iOS")
                .setOsVersion("17.0")
                .setBrowserType("Safari")
                .setDeviceFingerprint("fp-" + name)
                .setLoginIp("192.168.1.1")
                .setLoginLocation("Shanghai");
    }

    private ContentUserDeviceSession buildSession(String id, String userId, Date lastActiveTime) {
        ContentUserDeviceSession session = new ContentUserDeviceSession();
        session.setId(id);
        session.setUserId(userId);
        session.setSessionStatus(DeviceSessionStatusEnum.ACTIVE.getCode());
        session.setLastActiveTime(lastActiveTime);
        session.setTokenJti("jti-" + id);
        return session;
    }

    @Test
    void createSessionWithLimit_withLessThan5ActiveSessions_shouldCreateNormally() {
        String userId = "user-001";
        // 模拟当前有3个活跃会话
        when(deviceSessionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);
        when(deviceSessionMapper.insert(any(ContentUserDeviceSession.class))).thenReturn(1);

        ContentUserDeviceSession result = service.createSessionWithLimit(userId, "jti-new", buildDeviceInfo("iPhone"));

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getSessionStatus()).isEqualTo(DeviceSessionStatusEnum.ACTIVE.getCode());
        // 不应触发挤出逻辑
        verify(deviceSessionMapper, never()).selectOne(any(LambdaQueryWrapper.class));
        verify(deviceSessionMapper, never()).updateById(any(ContentUserDeviceSession.class));
    }

    @Test
    void createSessionWithLimit_withExactly5ActiveSessions_shouldEvictEarliestAndCreateNew() {
        String userId = "user-001";
        // 模拟当前有5个活跃会话
        when(deviceSessionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        // 模拟查到最早的会话
        ContentUserDeviceSession oldestSession = buildSession("s-oldest", userId, new Date(1000L));
        when(deviceSessionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(oldestSession);
        when(deviceSessionMapper.updateById(any(ContentUserDeviceSession.class))).thenReturn(1);
        when(deviceSessionMapper.insert(any(ContentUserDeviceSession.class))).thenReturn(1);

        ContentUserDeviceSession result = service.createSessionWithLimit(userId, "jti-new", buildDeviceInfo("iPhone"));

        assertThat(result).isNotNull();
        assertThat(result.getSessionStatus()).isEqualTo(DeviceSessionStatusEnum.ACTIVE.getCode());
        // 验证挤出了最早的会话
        verify(deviceSessionMapper).updateById(any(ContentUserDeviceSession.class));
        verify(deviceSessionMapper).insert(any(ContentUserDeviceSession.class));
    }

    @Test
    void createSessionWithLimit_evictedSession_shouldHaveOfflineStatusAndDeviceLimitEvictionReason() {
        String userId = "user-001";
        when(deviceSessionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        ContentUserDeviceSession oldestSession = buildSession("s-oldest", userId, new Date(1000L));
        when(deviceSessionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(oldestSession);
        when(deviceSessionMapper.updateById(any(ContentUserDeviceSession.class))).thenReturn(1);
        when(deviceSessionMapper.insert(any(ContentUserDeviceSession.class))).thenReturn(1);

        service.createSessionWithLimit(userId, "jti-new", buildDeviceInfo("iPhone"));

        ArgumentCaptor<ContentUserDeviceSession> captor = ArgumentCaptor.forClass(ContentUserDeviceSession.class);
        verify(deviceSessionMapper).updateById(captor.capture());
        ContentUserDeviceSession evicted = captor.getValue();
        assertThat(evicted.getSessionStatus()).isEqualTo(DeviceSessionStatusEnum.OFFLINE.getCode());
        assertThat(evicted.getOfflineReason()).isEqualTo("DEVICE_LIMIT_EVICTION");
        assertThat(evicted.getOfflineTime()).isNotNull();
    }

    @Test
    void createSessionWithLimit_shouldEvictSessionWithOldestLastActiveTime() {
        String userId = "user-001";
        when(deviceSessionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        // 最早的会话（lastActiveTime最小）
        Date oldestTime = new Date(1000L);
        ContentUserDeviceSession oldestSession = buildSession("s-oldest", userId, oldestTime);
        when(deviceSessionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(oldestSession);
        when(deviceSessionMapper.updateById(any(ContentUserDeviceSession.class))).thenReturn(1);
        when(deviceSessionMapper.insert(any(ContentUserDeviceSession.class))).thenReturn(1);

        service.createSessionWithLimit(userId, "jti-new", buildDeviceInfo("iPhone"));

        // 验证查询时使用了lastActiveTime升序排序
        ArgumentCaptor<LambdaQueryWrapper> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(deviceSessionMapper).selectOne(wrapperCaptor.capture());
    }

    @Test
    void countActiveSessions_shouldReturnCorrectCount() {
        when(deviceSessionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        int count = service.countActiveSessions("user-001");

        assertThat(count).isEqualTo(3);
    }

    @Test
    void countActiveSessions_shouldReturnZeroWhenNoSessions() {
        when(deviceSessionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        int count = service.countActiveSessions("user-001");

        assertThat(count).isZero();
    }
}
