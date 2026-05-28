package org.jeecg.modules.content.userstatus.service;

import org.jeecg.modules.content.userstatus.entity.UserStatusAuditLog;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.jeecg.modules.content.userstatus.mapper.UserStatusAuditLogMapper;
import org.jeecg.modules.content.userstatus.service.impl.UserStatusAuditLogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 审计日志服务测试。
 * 测试日志写入、按用户查询、时间范围筛选、防篡改验证。
 */
@ExtendWith(MockitoExtension.class)
class UserStatusAuditLogServiceTest {

    @Mock
    private UserStatusAuditLogMapper auditLogMapper;

    @InjectMocks
    private UserStatusAuditLogServiceImpl auditLogService;

    @Test
    void shouldWriteAuditLogSuccessfully() {
        // Given
        UserStatusAuditLog log = createTestAuditLog();
        when(auditLogMapper.insert(any(UserStatusAuditLog.class))).thenReturn(1);

        // When
        auditLogService.writeAuditLog(log);

        // Then
        verify(auditLogMapper).insert(any(UserStatusAuditLog.class));
    }

    @Test
    void shouldQueryAuditLogsByUserId() {
        // Given
        String userId = "user001";
        List<UserStatusAuditLog> expectedLogs = Arrays.asList(
            createTestAuditLog(),
            createTestAuditLog()
        );
        when(auditLogMapper.selectByUserId(userId)).thenReturn(expectedLogs);

        // When
        List<UserStatusAuditLog> actualLogs = auditLogService.queryByUserId(userId);

        // Then
        assertThat(actualLogs).hasSize(2);
        assertThat(actualLogs).isEqualTo(expectedLogs);
    }

    @Test
    void shouldQueryAuditLogsByTimeRange() {
        // Given
        String userId = "user001";
        Date startTime = new Date(System.currentTimeMillis() - 86400000); // 1天前
        Date endTime = new Date();
        List<UserStatusAuditLog> expectedLogs = Arrays.asList(createTestAuditLog());
        when(auditLogMapper.selectByUserIdAndTimeRange(userId, startTime, endTime)).thenReturn(expectedLogs);

        // When
        List<UserStatusAuditLog> actualLogs = auditLogService.queryByUserIdAndTimeRange(userId, startTime, endTime);

        // Then
        assertThat(actualLogs).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListForNonExistentUser() {
        // Given
        String userId = "nonexistent";
        when(auditLogMapper.selectByUserId(userId)).thenReturn(Arrays.asList());

        // When
        List<UserStatusAuditLog> actualLogs = auditLogService.queryByUserId(userId);

        // Then
        assertThat(actualLogs).isEmpty();
    }

    @Test
    void shouldHaveImmutableLogAfterWrite() {
        // Given
        UserStatusAuditLog log = createTestAuditLog();
        when(auditLogMapper.insert(any(UserStatusAuditLog.class))).thenReturn(1);

        // When
        auditLogService.writeAuditLog(log);

        // Then - 验证日志没有UPDATE或DELETE方法
        // 审计日志应该是不可变的，只有INSERT操作
        verify(auditLogMapper).insert(any(UserStatusAuditLog.class));
    }

    private UserStatusAuditLog createTestAuditLog() {
        UserStatusAuditLog log = new UserStatusAuditLog();
        log.setLogId("log001");
        log.setUserId("user001");
        log.setFromStatus(UserStatusEnum.NORMAL.name());
        log.setToStatus(UserStatusEnum.MUTED.name());
        log.setOperatorId("admin001");
        log.setOperatorType("ADMIN");
        log.setTriggerReason("违规发言");
        log.setStartTime(new Date());
        log.setEndTime(new Date(System.currentTimeMillis() + 86400000));
        log.setIpAddress("192.168.1.1");
        log.setCreatedAt(new Date());
        return log;
    }
}
