package org.jeecg.modules.content.channel.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.service.IChannelReviewService;
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
 * 频道定时任务 — 审核超时
 * 验证每 10 分钟扫描 24 小时未处理的待审核记录并标记 timeoutFlag=1
 */
@ExtendWith(MockitoExtension.class)
class ChannelScheduledTaskReviewTimeoutTest {

    @Mock
    private org.jeecg.modules.content.channel.service.ChannelService channelService;
    @Mock
    private org.jeecg.modules.content.channel.service.ChannelTransferService channelTransferService;
    @Mock
    private org.jeecg.modules.content.channel.biz.ScheduledPublishDispatchBiz scheduledPublishDispatchBiz;
    @Mock
    private IChannelReviewService channelReviewService;
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
    void should_not_update_when_no_pending_reviews() {
        when(channelReviewService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        task.processReviewTimeout();

        verify(channelReviewService, never()).updateBatchById(anyCollection());
    }

    @Test
    void should_mark_timeout_for_overdue_reviews() {
        ChannelReview review = new ChannelReview();
        review.setId("rv1");
        review.setStatus("pending");
        review.setTimeoutFlag(0);
        review.setSubmitTime(LocalDateTime.now().minusHours(48));
        when(channelReviewService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(review));

        task.processReviewTimeout();

        ArgumentCaptor<List<ChannelReview>> captor = ArgumentCaptor.forClass(List.class);
        verify(channelReviewService).updateBatchById(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getTimeoutFlag()).isEqualTo(1);
    }
}
