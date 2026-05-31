package org.jeecg.modules.content.circle.scheduler;

import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleRankingScheduler 测试")
class CircleRankingSchedulerTest {

    @Mock
    private CircleMapper circleMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CircleRankingScheduler scheduler;

    @Test
    @DisplayName("refreshRanking - 正常执行榜单刷新")
    void shouldRefreshRankingSuccessfully() {
        // Given
        when(circleMapper.selectHotCircles(20)).thenReturn(Collections.emptyList());
        when(circleMapper.selectNewCircles(20)).thenReturn(Collections.emptyList());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        scheduler.refreshRanking();

        // Then
        verify(circleMapper).selectHotCircles(20);
        verify(circleMapper).selectNewCircles(20);
        verify(valueOperations).set(eq("circle:ranking:hot"), any(), eq(2L), eq(TimeUnit.HOURS));
        verify(valueOperations).set(eq("circle:ranking:new"), any(), eq(2L), eq(TimeUnit.HOURS));
    }
}
