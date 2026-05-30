package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.mapper.CircleAuditLogMapper;
import org.jeecg.modules.content.circle.service.impl.CircleAuditLogServiceImpl;
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
 * 圈子审核日志服务测试。
 * 测试日志写入、按目标查询、时间范围筛选。
 */
@ExtendWith(MockitoExtension.class)
class CircleAuditLogServiceTest {

    @Mock
    private CircleAuditLogMapper circleAuditLogMapper;

    @InjectMocks
    private CircleAuditLogServiceImpl circleAuditLogService;

    @Test
    void writeAuditLog_shouldPersistLog() {
        // Given
        CircleAuditLog log = createTestAuditLog();
        when(circleAuditLogMapper.insert(any(CircleAuditLog.class))).thenReturn(1);

        // When
        circleAuditLogService.writeAuditLog(log);

        // Then
        verify(circleAuditLogMapper).insert(any(CircleAuditLog.class));
    }

    @Test
    void writeAuditLog_shouldAutoSetCreatedAt() {
        // Given
        CircleAuditLog log = createTestAuditLog();
        log.setCreatedAt(null);
        when(circleAuditLogMapper.insert(any(CircleAuditLog.class))).thenReturn(1);

        // When
        circleAuditLogService.writeAuditLog(log);

        // Then
        assertThat(log.getCreatedAt()).isNotNull();
        verify(circleAuditLogMapper).insert(any(CircleAuditLog.class));
    }

    @Test
    void queryByTimeRange_shouldReturnLogsInTimeRange() {
        // Given
        Date startTime = new Date(System.currentTimeMillis() - 86400000);
        Date endTime = new Date();
        List<CircleAuditLog> expectedLogs = Arrays.asList(createTestAuditLog());
        when(circleAuditLogMapper.selectByTimeRange(startTime, endTime)).thenReturn(expectedLogs);

        // When
        List<CircleAuditLog> actualLogs = circleAuditLogService.queryByTimeRange(startTime, endTime);

        // Then
        assertThat(actualLogs).hasSize(1);
        assertThat(actualLogs).isEqualTo(expectedLogs);
    }

    @Test
    void queryByTarget_shouldReturnLogsForTarget() {
        // Given
        String targetId = "content001";
        String targetType = "CONTENT";
        List<CircleAuditLog> expectedLogs = Arrays.asList(
            createTestAuditLog(),
            createTestAuditLog()
        );
        when(circleAuditLogMapper.selectByTarget(targetId, targetType)).thenReturn(expectedLogs);

        // When
        List<CircleAuditLog> actualLogs = circleAuditLogService.queryByTarget(targetId, targetType);

        // Then
        assertThat(actualLogs).hasSize(2);
        assertThat(actualLogs).isEqualTo(expectedLogs);
    }

    private CircleAuditLog createTestAuditLog() {
        CircleAuditLog log = new CircleAuditLog();
        log.setLogId("log001");
        log.setCircleId("circle001");
        log.setOperatorId("admin001");
        log.setAction("PIN");
        log.setTargetId("content001");
        log.setTargetType("CONTENT");
        log.setResult("SUCCESS");
        log.setReason("优质内容置顶");
        log.setCreatedAt(new Date());
        return log;
    }
}
