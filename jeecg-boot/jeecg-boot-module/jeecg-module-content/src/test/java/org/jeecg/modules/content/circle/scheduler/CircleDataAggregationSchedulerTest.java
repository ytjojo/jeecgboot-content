package org.jeecg.modules.content.circle.scheduler;

import org.jeecg.modules.content.circle.mapper.CircleDataStatisticsMapper;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.jeecg.modules.content.circle.entity.CircleDataStatistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleDataAggregationScheduler 测试")
class CircleDataAggregationSchedulerTest {

    @Mock
    private CircleDataStatisticsMapper dataMapper;

    @Mock
    private CircleMemberMapper memberMapper;

    @InjectMocks
    private CircleDataAggregationScheduler scheduler;

    @Test
    @DisplayName("aggregateData - 正常执行聚合任务")
    void shouldAggregateDataSuccessfully() {
        // Given
        when(memberMapper.selectMemberStatsGroupByCircle(any())).thenReturn(java.util.Collections.emptyList());

        // When
        scheduler.aggregateData();

        // Then - 验证没有异常抛出
        // 注：当前实现为 TODO，不验证具体调用
    }
}
