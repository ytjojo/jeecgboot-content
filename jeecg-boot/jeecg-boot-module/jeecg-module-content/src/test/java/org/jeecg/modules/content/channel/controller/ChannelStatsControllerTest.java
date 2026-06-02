package org.jeecg.modules.content.channel.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelStatsBiz;
import org.jeecg.modules.content.channel.constant.ChannelStatsConstant;
import org.jeecg.modules.content.channel.vo.ChannelStatsVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道统计控制器测试
 * 验证 core/trend/hotContent/userAnalysis 端点的委托 + 校验逻辑
 */
@ExtendWith(MockitoExtension.class)
class ChannelStatsControllerTest {

    @Mock
    private ChannelStatsBiz channelStatsBiz;

    @InjectMocks
    private ChannelStatsController controller;

    @Test
    void should_get_core_stats() {
        ChannelStatsVO vo = ChannelStatsVO.builder().channelId("ch1").subscriberCount(10).build();
        when(channelStatsBiz.getCoreStats("ch1")).thenReturn(vo);

        Result<ChannelStatsVO> result = controller.getCoreStats("ch1");

        assertThat(result.isSuccess()).isTrue();
        verify(channelStatsBiz).getCoreStats("ch1");
    }

    @Test
    void should_get_trend_data() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);
        when(channelStatsBiz.getTrendData("ch1", start, end, ChannelStatsConstant.STAT_TYPE_DAILY))
            .thenReturn(null);

        Result<?> result = controller.getTrendData("ch1", start, end, ChannelStatsConstant.STAT_TYPE_DAILY);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void should_reject_invalid_stat_type() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        Result<?> result = controller.getTrendData("ch1", start, end, "yearly");

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("无效的统计类型");
    }

    @Test
    void should_get_hot_content() {
        when(channelStatsBiz.getHotContent("ch1", 5, 7)).thenReturn(Collections.emptyList());

        Result<?> result = controller.getHotContent("ch1", 5, 7);

        assertThat(result.isSuccess()).isTrue();
        verify(channelStatsBiz).getHotContent("ch1", 5, 7);
    }

    @Test
    void should_get_user_analysis() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);
        when(channelStatsBiz.getUserAnalysis("ch1", start, end)).thenReturn(null);

        Result<?> result = controller.getUserAnalysis("ch1", start, end);

        assertThat(result.isSuccess()).isTrue();
    }
}
