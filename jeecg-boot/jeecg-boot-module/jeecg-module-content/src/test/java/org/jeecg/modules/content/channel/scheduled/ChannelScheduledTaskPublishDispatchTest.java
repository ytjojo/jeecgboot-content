package org.jeecg.modules.content.channel.scheduled;

import org.jeecg.modules.content.channel.biz.ScheduledPublishDispatchBiz;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * 频道定时任务 — 调度发布分发
 * 验证每分钟调用 ScheduledPublishDispatchBiz.dispatch()
 */
@ExtendWith(MockitoExtension.class)
class ChannelScheduledTaskPublishDispatchTest {

    @Mock
    private org.jeecg.modules.content.channel.service.ChannelService channelService;
    @Mock
    private org.jeecg.modules.content.channel.service.ChannelTransferService channelTransferService;
    @Mock
    private ScheduledPublishDispatchBiz scheduledPublishDispatchBiz;
    @Mock
    private org.jeecg.modules.content.channel.service.IChannelReviewService channelReviewService;
    @Mock
    private org.jeecg.modules.content.channel.service.IChannelExportTaskService exportTaskService;
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
    void should_dispatch_scheduled_publish() {
        assertDoesNotThrow(() -> task.processScheduledPublish());
        verify(scheduledPublishDispatchBiz).dispatch();
    }
}
