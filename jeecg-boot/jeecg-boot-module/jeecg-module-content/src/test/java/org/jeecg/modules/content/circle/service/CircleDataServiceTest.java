package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.entity.CircleDataStatistics;
import org.jeecg.modules.content.circle.mapper.CircleDataStatisticsMapper;
import org.jeecg.modules.content.circle.service.impl.CircleDataServiceImpl;
import org.jeecg.modules.content.circle.vo.CircleDataStatisticsVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleDataService 测试")
class CircleDataServiceTest {

    @Mock
    private CircleDataStatisticsMapper dataMapper;

    @InjectMocks
    private CircleDataServiceImpl circleDataService;

    @Test
    @DisplayName("getStatistics - 有数据时返回正确统计")
    void shouldReturnCorrectStatisticsWhenDataExists() {
        // Given
        String circleId = "test-circle-id";
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        CircleDataStatistics stat1 = new CircleDataStatistics()
                .setCircleId(circleId)
                .setStatDate(startDate.plusDays(1))
                .setMemberCount(100)
                .setNewMemberCount(10)
                .setPostCount(50)
                .setNewPostCount(5)
                .setActiveCount(30);

        CircleDataStatistics stat2 = new CircleDataStatistics()
                .setCircleId(circleId)
                .setStatDate(startDate.plusDays(2))
                .setMemberCount(110)
                .setNewMemberCount(12)
                .setPostCount(55)
                .setNewPostCount(8)
                .setActiveCount(35);

        List<CircleDataStatistics> stats = Arrays.asList(stat1, stat2);
        when(dataMapper.selectByCircleIdAndDateRange(circleId, startDate, endDate)).thenReturn(stats);

        // When
        CircleDataStatisticsVO result = circleDataService.getStatistics(circleId, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMemberCount()).isEqualTo(110); // 最新一天的成员数
        assertThat(result.getNewMemberCount()).isEqualTo(22); // 两天新增之和
        assertThat(result.getPostCount()).isEqualTo(55);
        assertThat(result.getNewPostCount()).isEqualTo(13);
        assertThat(result.getDailyTrends()).hasSize(2);
    }

    @Test
    @DisplayName("getStatistics - 无数据时返回空统计")
    void shouldReturnEmptyStatisticsWhenNoData() {
        // Given
        String circleId = "test-circle-id";
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(dataMapper.selectByCircleIdAndDateRange(circleId, startDate, endDate)).thenReturn(Collections.emptyList());

        // When
        CircleDataStatisticsVO result = circleDataService.getStatistics(circleId, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMemberCount()).isEqualTo(0);
        assertThat(result.getNewMemberCount()).isEqualTo(0);
        assertThat(result.getPostCount()).isEqualTo(0);
        assertThat(result.getNewPostCount()).isEqualTo(0);
        assertThat(result.getDailyTrends()).isEmpty();
    }
}
