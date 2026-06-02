package org.jeecg.modules.content.channel.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.entity.ChannelAppeal;
import org.jeecg.modules.content.channel.service.IChannelAppealService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * 频道定时任务 — 申诉 SLA 检查
 * 验证每日 9 点扫描 3 天未首次响应的申诉（仅告警，不修改数据）
 */
@ExtendWith(MockitoExtension.class)
class ChannelScheduledTaskAppealSlaTest {

    @Mock
    private org.jeecg.modules.content.channel.service.ChannelService channelService;
    @Mock
    private org.jeecg.modules.content.channel.service.ChannelTransferService channelTransferService;
    @Mock
    private org.jeecg.modules.content.channel.biz.ScheduledPublishDispatchBiz scheduledPublishDispatchBiz;
    @Mock
    private org.jeecg.modules.content.channel.service.IChannelReviewService channelReviewService;
    @Mock
    private org.jeecg.modules.content.channel.service.IChannelExportTaskService exportTaskService;
    @Mock
    private IChannelAppealService appealService;
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
    void should_run_without_exception_on_no_sla_violations() {
        when(appealService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> task.checkAppealSla());
    }

    @Test
    void should_run_with_sla_violations() {
        ChannelAppeal appeal = new ChannelAppeal();
        appeal.setId("a1");
        appeal.setStatus("pending");
        appeal.setCreatedTime(LocalDateTime.now().minusDays(5));
        when(appealService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(appeal));

        assertDoesNotThrow(() -> task.checkAppealSla());
    }
}
