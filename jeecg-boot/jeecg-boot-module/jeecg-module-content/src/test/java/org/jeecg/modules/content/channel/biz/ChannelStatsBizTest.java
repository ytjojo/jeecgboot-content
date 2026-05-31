package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelStats;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.service.ChannelSubscriptionService;
import org.jeecg.modules.content.channel.service.IChannelStatsService;
import org.jeecg.modules.content.channel.vo.ChannelHotContentVO;
import org.jeecg.modules.content.channel.vo.ChannelStatsVO;
import org.jeecg.modules.content.channel.vo.ChannelUserAnalysisVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelStatsBizTest {

    @Mock
    private IChannelStatsService statsService;

    @Mock
    private ChannelContentPublishMapper publishMapper;

    @Mock
    private ChannelSubscriptionService subscriptionService;

    @InjectMocks
    private ChannelStatsBiz channelStatsBiz;

    @Test
    void shouldGetCoreStats() {
        String channelId = "CH001";
        ChannelStats stats = new ChannelStats()
                .setChannelId(channelId)
                .setSubscriberCount(1000)
                .setContentCount(50)
                .setPv(50000L)
                .setUv(10000L)
                .setUpdatedTime(LocalDateTime.now());

        when(statsService.getLatestStats(channelId)).thenReturn(stats);

        ChannelStatsVO result = channelStatsBiz.getCoreStats(channelId);

        assertNotNull(result);
        assertEquals(1000, result.getSubscriberCount());
        assertEquals(50, result.getContentCount());
        assertEquals(50000L, result.getPv());
        assertEquals(10000L, result.getUv());
        verify(statsService).getLatestStats(channelId);
    }

    @Test
    void shouldReturnEmptyStatsWhenNoData() {
        String channelId = "CH001";
        when(statsService.getLatestStats(channelId)).thenReturn(null);

        ChannelStatsVO result = channelStatsBiz.getCoreStats(channelId);

        assertNotNull(result);
        assertEquals(0, result.getSubscriberCount());
        assertEquals(0, result.getContentCount());
    }

    @Test
    void shouldGetHotContentWithDefaultParams() {
        String channelId = "CH001";
        ChannelContentPublish publish = new ChannelContentPublish()
                .setChannelId(channelId)
                .setContentId("CONTENT001")
                .setContentType("article")
                .setPublisherId("USER001")
                .setPublishStatus("PUBLISHED");
        publish.setCreateTime(new Date());

        when(publishMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(publish));

        List<ChannelHotContentVO> result = channelStatsBiz.getHotContent(channelId, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CONTENT001", result.get(0).getContentId());
        assertEquals("article", result.get(0).getContentType());
        assertEquals(1, result.get(0).getRank());
    }

    @Test
    void shouldGetUserAnalysis() {
        String channelId = "CH001";
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);

        when(subscriptionService.count(any(LambdaQueryWrapper.class))).thenReturn(42L);

        ChannelUserAnalysisVO result = channelStatsBiz.getUserAnalysis(channelId, start, end);

        assertNotNull(result);
        assertEquals(42, result.getNewSubscriberCount());
        assertEquals(0, result.getLostSubscriberCount());
        assertNotNull(result.getActivityDistribution());
        assertNotNull(result.getContributionRanking());
    }
}
