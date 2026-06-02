package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.entity.ChannelStats;
import org.jeecg.modules.content.channel.mapper.ChannelStatsMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelStatsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 频道统计服务测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelStatsServiceTest {

    @Mock
    private ChannelStatsMapper statsMapper;

    @InjectMocks
    private ChannelStatsServiceImpl statsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(statsService, "baseMapper", statsMapper);
    }

    @Test
    void should_get_latest_stats() {
        ChannelStats stat = new ChannelStats();
        stat.setChannelId("ch1");
        when(statsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(stat);

        ChannelStats result = statsService.getLatestStats("ch1");

        assertThat(result).isNotNull();
        assertThat(result.getChannelId()).isEqualTo("ch1");
    }

    @Test
    void should_return_null_when_no_latest_stats() {
        when(statsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThat(statsService.getLatestStats("nope")).isNull();
    }

    @Test
    void should_delegate_trend_data_to_mapper() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);
        ChannelStats s1 = new ChannelStats();
        s1.setStatDate(start);
        when(statsMapper.selectTrendData("ch1", start, end, "daily")).thenReturn(List.of(s1));

        List<ChannelStats> result = statsService.getTrendData("ch1", start, end, "daily");

        assertThat(result).hasSize(1);
        verify(statsMapper).selectTrendData("ch1", start, end, "daily");
    }
}
