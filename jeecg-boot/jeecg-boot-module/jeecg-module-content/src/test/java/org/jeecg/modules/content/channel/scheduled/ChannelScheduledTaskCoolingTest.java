package org.jeecg.modules.content.channel.scheduled;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.jeecg.modules.content.channel.LambdaCacheInit;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 频道定时任务 — 冷静期到期
 * 验证每小时扫描处于 DELETE_COOLING 且到期频道并置为 DELETED
 */
@ExtendWith(MockitoExtension.class)
class ChannelScheduledTaskCoolingTest {

    @BeforeAll
    static void warmLambdaCache() {
        LambdaCacheInit.init(Channel.class);
    }

    @Mock
    private ChannelService channelService;
    @Mock
    private org.jeecg.modules.content.channel.service.ChannelTransferService channelTransferService;
    @Mock
    private org.jeecg.modules.content.channel.biz.ScheduledPublishDispatchBiz scheduledPublishDispatchBiz;
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
    void should_run_cooling_processing() {
        assertDoesNotThrow(() -> task.processDeleteCoolingExpired());
    }

    @Test
    void should_invoke_update_for_cooling_expired_channels() {
        when(channelService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);

        task.processDeleteCoolingExpired();

        verify(channelService).update(any(LambdaUpdateWrapper.class));
    }

    @Test
    void should_not_throw_when_no_expired_channels() {
        when(channelService.update(any(LambdaUpdateWrapper.class))).thenReturn(false);

        assertDoesNotThrow(() -> task.processDeleteCoolingExpired());
    }

    /**
     * Sanity: ChannelStatus.DELETE_COOLING 仍然存在枚举值，确保被用到的方法签名稳定
     */
    @Test
    void delete_cooling_status_defined() {
        assertDoesNotThrow(() -> ChannelStatus.DELETE_COOLING.getCode());
    }
}
