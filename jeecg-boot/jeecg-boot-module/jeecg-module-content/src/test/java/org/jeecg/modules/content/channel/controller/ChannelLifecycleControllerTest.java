package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelLifecycleBiz;
import org.jeecg.modules.content.channel.entity.ChannelAppeal;
import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.req.ChannelLifecycleActionReq;
import org.jeecg.modules.content.channel.req.ChannelLifecycleLogQueryReq;
import org.jeecg.modules.content.channel.req.ChannelAppealHandleReq;
import org.jeecg.modules.content.channel.req.ChannelAppealSubmitReq;
import org.jeecg.modules.content.channel.service.IChannelAppealService;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 频道生命周期控制器测试
 * 验证生命周期操作、审计日志、申诉流程的委托行为
 */
@ExtendWith(MockitoExtension.class)
class ChannelLifecycleControllerTest {

    @Mock
    private ChannelLifecycleBiz lifecycleBiz;
    @Mock
    private IChannelLifecycleLogService lifecycleLogService;
    @Mock
    private IChannelAppealService appealService;
    @Mock
    private IContentNotificationService notificationService;

    @InjectMocks
    private ChannelLifecycleController controller;

    @Test
    void should_freeze_channel() {
        ChannelLifecycleActionReq req = new ChannelLifecycleActionReq();
        req.setChannelId("ch1");
        req.setReason("violation");

        Result<Void> result = controller.freeze(req);

        assertThat(result.isSuccess()).isTrue();
        verify(lifecycleBiz).freeze("ch1", "current-user-id", "violation");
    }

    @Test
    void should_unfreeze_channel() {
        ChannelLifecycleActionReq req = new ChannelLifecycleActionReq();
        req.setChannelId("ch1");
        req.setReason("restored");

        Result<Void> result = controller.unfreeze(req);

        assertThat(result.isSuccess()).isTrue();
        verify(lifecycleBiz).unfreeze("ch1", "current-user-id", "restored");
    }

    @Test
    void should_hide_channel() {
        ChannelLifecycleActionReq req = new ChannelLifecycleActionReq();
        req.setChannelId("ch1");

        controller.hide(req);
        verify(lifecycleBiz).hide("ch1", "current-user-id", null);
    }

    @Test
    void should_close_channel() {
        ChannelLifecycleActionReq req = new ChannelLifecycleActionReq();
        req.setChannelId("ch1");

        controller.close(req);
        verify(lifecycleBiz).close("ch1", "current-user-id", null);
    }

    @Test
    void should_archive_channel() {
        ChannelLifecycleActionReq req = new ChannelLifecycleActionReq();
        req.setChannelId("ch1");

        controller.archive(req);
        verify(lifecycleBiz).archive("ch1", "current-user-id", null);
    }

    @Test
    void should_restrict_recommend() {
        ChannelLifecycleActionReq req = new ChannelLifecycleActionReq();
        req.setChannelId("ch1");

        controller.restrictRecommend(req);
        verify(lifecycleBiz).restrictRecommend("ch1", "current-user-id", null);
    }

    @Test
    void should_query_logs_with_filters() {
        ChannelLifecycleLogQueryReq req = new ChannelLifecycleLogQueryReq();
        req.setChannelId("ch1");
        req.setOperatorId("op1");
        req.setActionType("freeze");
        req.setPageNum(1);
        req.setPageSize(10);

        IPage<ChannelLifecycleLog> empty = new Page<>(1, 10);
        empty.setRecords(Collections.emptyList());
        doReturn(empty).when(lifecycleLogService).page(any(Page.class), any());

        Result<IPage<ChannelLifecycleLog>> result = controller.queryLogs(req);

        assertThat(result.isSuccess()).isTrue();
        ArgumentCaptor<Page<ChannelLifecycleLog>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(lifecycleLogService).page(pageCaptor.capture(), any());
        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(1);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(10);
    }

    @Test
    void should_submit_appeal() {
        ChannelAppealSubmitReq req = new ChannelAppealSubmitReq();
        req.setChannelId("ch1");
        req.setLifecycleLogId("log1");
        req.setAppealReason("unfair");

        ChannelAppeal appeal = new ChannelAppeal();
        appeal.setId("app1");
        when(appealService.submitAppeal("ch1", "log1", "current-user-id", "unfair", null))
            .thenReturn(appeal);

        Result<?> result = controller.submitAppeal(req);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void should_handle_appeal_with_notification() {
        ChannelAppealHandleReq req = new ChannelAppealHandleReq();
        req.setAppealId("app1");
        req.setAction("approved");
        req.setHandleResult("restore");

        ChannelAppeal appeal = new ChannelAppeal();
        appeal.setId("app1");
        appeal.setChannelId("ch1");
        appeal.setApplicantId("user1");
        when(appealService.handleAppeal("app1", "current-user-id", "approved", "restore"))
            .thenReturn(appeal);

        Result<?> result = controller.handleAppeal(req);

        assertThat(result.isSuccess()).isTrue();
        verify(notificationService).sendNotification(eq("user1"), eq("channel_appeal"), any(), any());
    }

    @Test
    void should_query_appeals() {
        IPage<ChannelAppeal> empty = new Page<>(1, 10);
        doReturn(empty).when(appealService).page(any(Page.class), any());

        Result<IPage<ChannelAppeal>> result = controller.queryAppeals("ch1", "pending", 1, 10);

        assertThat(result.isSuccess()).isTrue();
        verify(appealService).page(any(Page.class), any());
    }
}
