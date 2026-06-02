package org.jeecg.modules.content.channel.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.LambdaCacheInit;
import org.jeecg.modules.content.channel.biz.ChannelLifecycleBiz;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 频道定时任务 — 不活跃频道扫描（9.1-9.4）
 * 验证：
 *  - 首次发现：发送提醒 + 写日志
 *  - 二次扫描：个人频道自动归档
 *  - 二次扫描：组织频道被收集（仅日志）
 *  - 边界：6 个月内活跃 / 已是终态
 */
@ExtendWith(MockitoExtension.class)
class ChannelScheduledTaskInactiveScanTest {

    @BeforeAll
    static void warmLambdaCache() {
        LambdaCacheInit.init(Channel.class);
        LambdaCacheInit.init(ChannelContentPublish.class);
        LambdaCacheInit.init(ChannelLifecycleLog.class);
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
    private ChannelContentPublishMapper publishMapper;
    @Mock
    private IChannelLifecycleLogService lifecycleLogService;
    @Mock
    private ChannelLifecycleBiz lifecycleBiz;
    @Mock
    private IContentNotificationService notificationService;

    @InjectMocks
    private ChannelScheduledTask task;

    @Test
    void should_not_throw_when_no_inactive_channels() {
        when(publishMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(channelService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> task.scanInactiveChannels());
        verifyNoInteractions(notificationService);
    }

    @Test
    void should_send_reminder_on_first_discovery_of_personal_channel() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setName("Personal Ch");
        ch.setOwnerId("user1");
        ch.setChannelType(ChannelType.PERSONAL);
        when(publishMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(channelService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(ch));
        // 没有提醒日志
        when(lifecycleLogService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        task.scanInactiveChannels();

        verify(notificationService).sendNotification(eq("user1"), eq("channel_inactive_remind"), anyString(), anyString());
        verify(lifecycleLogService).save(any(ChannelLifecycleLog.class));
    }

    @Test
    void should_skip_ownerless_channel() {
        Channel ch = new Channel();
        ch.setId("ch-no-owner");
        ch.setOwnerId(null);
        ch.setChannelType(ChannelType.PERSONAL);
        when(publishMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(channelService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(ch));
        when(lifecycleLogService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        task.scanInactiveChannels();

        verifyNoInteractions(notificationService);
    }

    @Test
    void should_archive_personal_channel_when_reminder_expired_and_still_inactive() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setName("Personal");
        ch.setOwnerId("user1");
        ch.setChannelType(ChannelType.PERSONAL);
        when(publishMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(channelService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(ch));
        // 已有提醒日志
        ChannelLifecycleLog remindLog = new ChannelLifecycleLog();
        remindLog.setChannelId("ch1");
        remindLog.setActionType("inactivity_remind");
        when(lifecycleLogService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(remindLog));
        // 7 个月内无新活动
        when(publishMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        task.scanInactiveChannels();

        verify(lifecycleBiz).archive(eq("ch1"), eq("system"), anyString());
    }

    @Test
    void should_record_organization_inactive_warning_only() {
        Channel ch = new Channel();
        ch.setId("org1");
        ch.setName("OrgChannel");
        ch.setOwnerId("user1");
        ch.setChannelType(ChannelType.ORGANIZATION);
        when(publishMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(channelService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(ch));
        ChannelLifecycleLog remindLog = new ChannelLifecycleLog();
        remindLog.setChannelId("org1");
        remindLog.setActionType("inactivity_remind");
        when(lifecycleLogService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(remindLog));
        when(publishMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        task.scanInactiveChannels();

        verify(lifecycleBiz, never()).archive(anyString(), anyString(), anyString());
    }

    @Test
    void should_swallow_archive_exception_and_continue() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setOwnerId("u1");
        ch.setChannelType(ChannelType.PERSONAL);
        when(publishMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(channelService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(ch));
        ChannelLifecycleLog remindLog = new ChannelLifecycleLog();
        remindLog.setChannelId("ch1");
        when(lifecycleLogService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(remindLog));
        when(publishMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        doThrow(new RuntimeException("db down")).when(lifecycleBiz)
            .archive(eq("ch1"), eq("system"), anyString());

        assertDoesNotThrow(() -> task.scanInactiveChannels());
    }

    @Test
    void should_skip_already_active_channels_within_six_months() {
        ChannelContentPublish recent = new ChannelContentPublish();
        recent.setChannelId("active1");
        when(publishMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(recent));
        Channel ch = new Channel();
        ch.setId("active1");
        when(channelService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(ch));

        task.scanInactiveChannels();

        verifyNoInteractions(notificationService);
        verify(lifecycleLogService, never()).save(any(ChannelLifecycleLog.class));
    }

    @Test
    void should_write_inactivity_remind_log_with_correct_action_type() {
        Channel ch = new Channel();
        ch.setId("ch1");
        ch.setOwnerId("u1");
        ch.setChannelType(ChannelType.PERSONAL);
        when(publishMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(channelService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(ch));
        when(lifecycleLogService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        task.scanInactiveChannels();

        ArgumentCaptor<ChannelLifecycleLog> captor = ArgumentCaptor.forClass(ChannelLifecycleLog.class);
        verify(lifecycleLogService).save(captor.capture());
        assertThat(captor.getValue().getActionType()).isEqualTo("inactivity_remind");
        assertThat(captor.getValue().getChannelId()).isEqualTo("ch1");
    }
}
