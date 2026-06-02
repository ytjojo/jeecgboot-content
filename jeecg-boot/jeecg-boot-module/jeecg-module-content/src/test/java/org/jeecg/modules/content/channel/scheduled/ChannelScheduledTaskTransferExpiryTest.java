package org.jeecg.modules.content.channel.scheduled;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.jeecg.modules.content.channel.LambdaCacheInit;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;
import org.jeecg.modules.content.channel.service.ChannelTransferService;
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
 * 频道定时任务 — 转让超时
 * 验证每小时扫描 PENDING 但 expireTime 到期的转让，置为 EXPIRED
 */
@ExtendWith(MockitoExtension.class)
class ChannelScheduledTaskTransferExpiryTest {

    @BeforeAll
    static void warmLambdaCache() {
        LambdaCacheInit.init(ChannelTransfer.class);
    }

    @Mock
    private org.jeecg.modules.content.channel.service.ChannelService channelService;
    @Mock
    private ChannelTransferService channelTransferService;
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
    void should_run_transfer_expiry() {
        assertDoesNotThrow(() -> task.processTransferExpired());
    }

    @Test
    void should_invoke_update_on_expired_transfers() {
        when(channelTransferService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);

        task.processTransferExpired();

        verify(channelTransferService).update(any(LambdaUpdateWrapper.class));
    }

    @Test
    void should_not_throw_when_no_expired_transfers() {
        when(channelTransferService.update(any(LambdaUpdateWrapper.class))).thenReturn(false);
        assertDoesNotThrow(() -> task.processTransferExpired());
    }
}
