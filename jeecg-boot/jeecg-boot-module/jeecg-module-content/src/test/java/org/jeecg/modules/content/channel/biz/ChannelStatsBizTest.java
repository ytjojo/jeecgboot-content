package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.entity.ChannelStats;
import org.jeecg.modules.content.channel.service.IChannelStatsService;
import org.jeecg.modules.content.channel.vo.ChannelStatsVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelStatsBizTest {

    @Mock
    private IChannelStatsService statsService;

    @InjectMocks
    private ChannelStatsBiz channelStatsBiz;

    @Test
    void shouldGetCoreStats() {
        // Given
        String channelId = "CH001";
        ChannelStats stats = new ChannelStats()
                .setChannelId(channelId)
                .setSubscriberCount(1000)
                .setContentCount(50)
                .setPv(50000L)
                .setUv(10000L)
                .setUpdatedTime(LocalDateTime.now());

        when(statsService.getLatestStats(channelId)).thenReturn(stats);

        // When
        ChannelStatsVO result = channelStatsBiz.getCoreStats(channelId);

        // Then
        assertNotNull(result);
        assertEquals(1000, result.getSubscriberCount());
        assertEquals(50, result.getContentCount());
        assertEquals(50000L, result.getPv());
        assertEquals(10000L, result.getUv());
        verify(statsService).getLatestStats(channelId);
    }

    @Test
    void shouldReturnEmptyStatsWhenNoData() {
        // Given
        String channelId = "CH001";
        when(statsService.getLatestStats(channelId)).thenReturn(null);

        // When
        ChannelStatsVO result = channelStatsBiz.getCoreStats(channelId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getSubscriberCount());
        assertEquals(0, result.getContentCount());
    }
}
