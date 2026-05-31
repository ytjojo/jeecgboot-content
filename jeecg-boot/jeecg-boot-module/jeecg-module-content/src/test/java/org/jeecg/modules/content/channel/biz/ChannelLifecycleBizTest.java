package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.enums.ChannelLifecycleStatus;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelLifecycleBizTest {

    @Mock
    private IChannelLifecycleLogService lifecycleLogService;

    @InjectMocks
    private ChannelLifecycleBiz lifecycleBiz;

    @Test
    void shouldAllowFreezeFromActive() {
        // Given
        String channelId = "CH001";
        String operatorId = "USER001";
        String reason = "违规内容";

        when(lifecycleLogService.save(any(ChannelLifecycleLog.class))).thenReturn(true);

        // When
        assertDoesNotThrow(() -> lifecycleBiz.freeze(channelId, operatorId, reason));

        // Then
        verify(lifecycleLogService).save(any(ChannelLifecycleLog.class));
    }

    @Test
    void shouldThrowWhenFreezeFromInvalidStatus() {
        // Given
        String channelId = "CH001";
        String operatorId = "USER001";
        String reason = "违规内容";

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> lifecycleBiz.freeze(channelId, operatorId, reason, ChannelLifecycleStatus.ARCHIVED));
    }

    @Test
    void shouldAllowUnfreezeFromFrozen() {
        // Given
        String channelId = "CH001";
        String operatorId = "USER001";
        String reason = "整改完成";

        when(lifecycleLogService.save(any(ChannelLifecycleLog.class))).thenReturn(true);

        // When
        assertDoesNotThrow(() -> lifecycleBiz.unfreeze(channelId, operatorId, reason));

        // Then
        verify(lifecycleLogService).save(any(ChannelLifecycleLog.class));
    }
}
