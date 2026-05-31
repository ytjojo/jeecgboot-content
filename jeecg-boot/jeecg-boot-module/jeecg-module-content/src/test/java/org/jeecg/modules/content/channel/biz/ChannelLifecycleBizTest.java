package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.enums.ChannelLifecycleStatus;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelLifecycleBizTest {

    @Mock
    private IChannelLifecycleLogService lifecycleLogService;

    @Mock
    private ChannelService channelService;

    @Mock
    private IContentNotificationService notificationService;

    @InjectMocks
    private ChannelLifecycleBiz lifecycleBiz;

    @Test
    void shouldAllowFreezeFromActive() {
        // Given: 频道无历史日志，默认 ACTIVE 状态
        String channelId = "CH001";
        Channel channel = new Channel().setName("测试频道").setOwnerId("owner-001");
        when(lifecycleLogService.getOne(any())).thenReturn(null);
        when(lifecycleLogService.save(any(ChannelLifecycleLog.class))).thenReturn(true);
        when(channelService.getById(channelId)).thenReturn(channel);

        // When
        assertDoesNotThrow(() -> lifecycleBiz.freeze(channelId, "USER001", "违规内容"));

        // Then
        verify(lifecycleLogService).save(any(ChannelLifecycleLog.class));
        verify(notificationService).sendNotification(eq("owner-001"), eq("channel_freeze"), anyString(), anyString());
    }

    @Test
    void shouldThrowWhenFreezeFromArchivedStatus() {
        // Given: 频道当前为 ARCHIVED 状态
        String channelId = "CH001";
        ChannelLifecycleLog existingLog = new ChannelLifecycleLog()
                .setChannelId(channelId)
                .setToStatus(ChannelLifecycleStatus.ARCHIVED.getCode());
        when(lifecycleLogService.getOne(any())).thenReturn(existingLog);

        // When & Then: ARCHIVED 不在 FREEZE_ALLOWED_FROM 中
        assertThrows(IllegalStateException.class,
                () -> lifecycleBiz.freeze(channelId, "USER001", "违规内容"));
    }

    @Test
    void shouldAllowUnfreezeFromFrozen() {
        // Given: 频道当前为 READONLY_FROZEN 状态
        String channelId = "CH001";
        ChannelLifecycleLog existingLog = new ChannelLifecycleLog()
                .setChannelId(channelId)
                .setToStatus(ChannelLifecycleStatus.READONLY_FROZEN.getCode());
        when(lifecycleLogService.getOne(any())).thenReturn(existingLog);
        when(lifecycleLogService.save(any(ChannelLifecycleLog.class))).thenReturn(true);

        // When
        assertDoesNotThrow(() -> lifecycleBiz.unfreeze(channelId, "USER001", "整改完成"));

        // Then
        verify(lifecycleLogService).save(any(ChannelLifecycleLog.class));
    }
}
