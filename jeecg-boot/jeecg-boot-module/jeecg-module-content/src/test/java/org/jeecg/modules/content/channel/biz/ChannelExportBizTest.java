package org.jeecg.modules.content.channel.biz;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.jeecg.modules.content.channel.entity.ChannelExportTask;
import org.jeecg.modules.content.channel.req.ChannelExportReq;
import org.jeecg.modules.content.channel.service.IChannelExportTaskService;
import org.jeecg.modules.content.channel.vo.ChannelExportTaskVO;
import org.jeecg.modules.content.channel.vo.ChannelStatsVO;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelExportBizTest {

    @Mock
    private IChannelExportTaskService exportTaskService;

    @Mock
    private ChannelStatsBiz channelStatsBiz;

    @InjectMocks
    private ChannelExportBiz exportBiz;

    private String tempUploadPath;

    @BeforeEach
    void setUp() {
        tempUploadPath = System.getProperty("java.io.tmpdir") + "/channel-export-test-" + System.nanoTime();
        ReflectionTestUtils.setField(exportBiz, "uploadPath", tempUploadPath);
    }

    private void cleanupTempDir() {
        if (tempUploadPath != null) {
            File dir = new File(tempUploadPath);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) f.delete();
                }
                dir.delete();
            }
        }
    }

    @Test
    void shouldCreateExportTaskWithDefaultFormat() {
        // Given
        ChannelExportReq req = new ChannelExportReq();
        req.setChannelId("ch-001");
        req.setExportType("core_stats");

        ChannelStatsVO statsVO = ChannelStatsVO.builder()
                .channelId("ch-001")
                .subscriberCount(100)
                .contentCount(50)
                .build();
        when(channelStatsBiz.getCoreStats("ch-001")).thenReturn(statsVO);
        when(exportTaskService.save(any())).thenAnswer(invocation -> {
            ChannelExportTask task = invocation.getArgument(0);
            task.setId("generated-id");
            return true;
        });
        when(exportTaskService.updateById(any())).thenReturn(true);
        when(exportTaskService.getById("generated-id")).thenAnswer(invocation -> {
            ChannelExportTask task = new ChannelExportTask();
            task.setId("generated-id");
            task.setTaskId("generated-id");
            task.setChannelId("ch-001");
            task.setExportType("core_stats");
            task.setFileFormat("xlsx");
            task.setStatus("pending");
            return task;
        });

        // When
        ChannelExportTaskVO result = exportBiz.createExport(req, "user-001");

        // Then
        assertNotNull(result);
        assertEquals("pending", result.getStatus());
        verify(exportTaskService).save(any(ChannelExportTask.class));
    }

    @Test
    void shouldCreateExportTaskWithCsvFormat() {
        // Given
        ChannelExportReq req = new ChannelExportReq();
        req.setChannelId("ch-001");
        req.setExportType("core_stats");
        req.setFileFormat("csv");

        ChannelStatsVO statsVO = ChannelStatsVO.builder()
                .channelId("ch-001")
                .subscriberCount(100)
                .contentCount(50)
                .build();
        when(channelStatsBiz.getCoreStats("ch-001")).thenReturn(statsVO);
        when(exportTaskService.save(any())).thenAnswer(invocation -> {
            ChannelExportTask task = invocation.getArgument(0);
            task.setId("generated-id");
            return true;
        });
        when(exportTaskService.updateById(any())).thenReturn(true);
        when(exportTaskService.getById("generated-id")).thenAnswer(invocation -> {
            ChannelExportTask task = new ChannelExportTask();
            task.setId("generated-id");
            task.setTaskId("generated-id");
            task.setChannelId("ch-001");
            task.setExportType("core_stats");
            task.setFileFormat("csv");
            task.setStatus("pending");
            return task;
        });

        // When
        ChannelExportTaskVO result = exportBiz.createExport(req, "user-001");

        // Then
        assertNotNull(result);
        verify(exportTaskService).save(any(ChannelExportTask.class));
    }

    @Test
    void shouldHandleProcessExportWithNullTask() {
        // Given
        when(exportTaskService.getById("nonexistent")).thenReturn(null);

        // When / Then - should not throw
        assertDoesNotThrow(() -> exportBiz.processExport("nonexistent"));
        verify(exportTaskService, never()).updateById(any());
        cleanupTempDir();
    }

    @Test
    void shouldSetFailedStatusOnExportError() {
        // Given - 使用无效导出类型触发异常
        ChannelExportTask task = new ChannelExportTask();
        task.setId("task-001");
        task.setTaskId("task-001");
        task.setChannelId("ch-001");
        task.setExportType("invalid_type");
        task.setFileFormat("xlsx");
        task.setStatus("pending");
        when(exportTaskService.getById("task-001")).thenReturn(task);
        when(exportTaskService.updateById(any())).thenReturn(true);

        // When
        exportBiz.processExport("task-001");

        // Then - 应设置为 failed 状态
        verify(exportTaskService, atLeastOnce()).updateById(argThat(t ->
                "failed".equals(((ChannelExportTask) t).getStatus())
        ));
        cleanupTempDir();
    }

    @Test
    void shouldSetCompletedStatusOnSuccessfulExport() {
        // Given - 使用有效的 core_stats 类型
        ChannelExportTask task = new ChannelExportTask();
        task.setId("task-002");
        task.setTaskId("task-002");
        task.setChannelId("ch-001");
        task.setExportType("core_stats");
        task.setFileFormat("xlsx");
        task.setStatus("pending");

        ChannelStatsVO statsVO = ChannelStatsVO.builder()
                .channelId("ch-001")
                .subscriberCount(100)
                .contentCount(50)
                .build();
        when(exportTaskService.getById("task-002")).thenReturn(task);
        when(channelStatsBiz.getCoreStats("ch-001")).thenReturn(statsVO);
        when(exportTaskService.updateById(any())).thenReturn(true);

        // When
        exportBiz.processExport("task-002");

        // Then - 应设置为 completed 状态
        verify(exportTaskService, atLeastOnce()).updateById(argThat(t ->
                "completed".equals(((ChannelExportTask) t).getStatus())
        ));
        cleanupTempDir();
    }

    @Test
    void shouldCalculateExpireTimeOnCompletion() {
        // Given
        ChannelExportTask task = new ChannelExportTask();
        task.setId("task-003");
        task.setTaskId("task-003");
        task.setChannelId("ch-001");
        task.setExportType("core_stats");
        task.setFileFormat("csv");
        task.setStatus("pending");

        ChannelStatsVO statsVO = ChannelStatsVO.builder()
                .channelId("ch-001")
                .subscriberCount(100)
                .contentCount(50)
                .build();
        when(exportTaskService.getById("task-003")).thenReturn(task);
        when(channelStatsBiz.getCoreStats("ch-001")).thenReturn(statsVO);
        when(exportTaskService.updateById(any())).thenReturn(true);

        // When
        exportBiz.processExport("task-003");

        // Then - 应设置过期时间
        verify(exportTaskService, atLeastOnce()).updateById(argThat(t -> {
            ChannelExportTask exportTask = (ChannelExportTask) t;
            return "completed".equals(exportTask.getStatus())
                    && exportTask.getExpireTime() != null
                    && exportTask.getExpireTime().isAfter(LocalDateTime.now().plusDays(6));
        }));
        cleanupTempDir();
    }
}
