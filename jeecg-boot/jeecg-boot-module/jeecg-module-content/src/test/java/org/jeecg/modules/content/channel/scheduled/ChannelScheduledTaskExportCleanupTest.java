package org.jeecg.modules.content.channel.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.entity.ChannelExportTask;
import org.jeecg.modules.content.channel.service.IChannelExportTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

/**
 * 频道定时任务 — 过期导出文件清理
 * 验证每小时清理过期 completed 任务（文件删除 + 状态置 expired）
 */
@ExtendWith(MockitoExtension.class)
class ChannelScheduledTaskExportCleanupTest {

    @Mock
    private org.jeecg.modules.content.channel.service.ChannelService channelService;
    @Mock
    private org.jeecg.modules.content.channel.service.ChannelTransferService channelTransferService;
    @Mock
    private org.jeecg.modules.content.channel.biz.ScheduledPublishDispatchBiz scheduledPublishDispatchBiz;
    @Mock
    private org.jeecg.modules.content.channel.service.IChannelReviewService channelReviewService;
    @Mock
    private IChannelExportTaskService exportTaskService;
    @Mock
    private org.jeecg.modules.content.channel.service.IChannelAppealService appealService;
    @Mock
    private org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper publishMapper;
    @Mock
    private org.jeecg.modules.content.channel.service.IChannelLifecycleLogService lifecycleLogService;
    @Mock
    private org.jeecg.modules.content.channel.biz.ChannelLifecycleBiz lifecycleBiz;
    @Mock
    private org.jeecg.modules.content.user.service.IContentNotificationService notificationService;

    @InjectMocks
    private ChannelScheduledTask task;

    @Test
    void should_not_update_when_no_expired_exports() {
        when(exportTaskService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        task.cleanupExpiredExports();

        verify(exportTaskService, never()).updateBatchById(anyCollection());
    }

    @Test
    void should_mark_expired_tasks() {
        ChannelExportTask task1 = new ChannelExportTask();
        task1.setId("et1");
        task1.setStatus("completed");
        task1.setFilePath(null);
        task1.setExpireTime(LocalDateTime.now().minusDays(1));
        when(exportTaskService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(task1));

        task.cleanupExpiredExports();

        ArgumentCaptor<List<ChannelExportTask>> captor = ArgumentCaptor.forClass(List.class);
        verify(exportTaskService).updateBatchById(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getStatus()).isEqualTo("expired");
    }
}
