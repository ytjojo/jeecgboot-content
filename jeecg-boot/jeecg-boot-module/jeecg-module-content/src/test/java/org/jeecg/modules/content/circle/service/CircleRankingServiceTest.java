package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.jeecg.modules.content.circle.service.impl.CircleRankingServiceImpl;
import org.jeecg.modules.content.circle.vo.CircleRankingVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleRankingService 测试")
class CircleRankingServiceTest {

    @Mock
    private CircleMapper circleMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CircleRankingServiceImpl rankingService;

    @Test
    @DisplayName("getHotRanking - 缓存未命中时从数据库查询")
    void shouldReturnHotRankingFromDatabaseWhenCacheMiss() {
        // Given
        Circle circle1 = new Circle();
        circle1.setId("circle-1");
        circle1.setName("技术圈");
        circle1.setMemberCount(1000);
        circle1.setCategory("技术");
        circle1.setDescription("技术交流");

        Circle circle2 = new Circle();
        circle2.setId("circle-2");
        circle2.setName("设计圈");
        circle2.setMemberCount(500);
        circle2.setCategory("设计");
        circle2.setDescription("设计交流");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("circle:ranking:hot")).thenReturn(null);
        when(circleMapper.selectHotCircles(100)).thenReturn(Arrays.asList(circle1, circle2));

        // When
        CircleRankingVO result = rankingService.getHotRanking(20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("HOT");
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getRank()).isEqualTo(1);
        assertThat(result.getItems().get(0).getCircleId()).isEqualTo("circle-1");
        assertThat(result.getItems().get(1).getRank()).isEqualTo(2);
    }

    @Test
    @DisplayName("getHotRanking - 缓存命中时直接返回缓存数据")
    void shouldReturnHotRankingFromCache() {
        // Given
        Circle circle1 = new Circle();
        circle1.setId("cached-circle-1");
        circle1.setName("缓存圈子");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("circle:ranking:hot")).thenReturn(Collections.singletonList(circle1));

        // When
        CircleRankingVO result = rankingService.getHotRanking(20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("HOT");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getCircleId()).isEqualTo("cached-circle-1");
    }

    @Test
    @DisplayName("getHotRanking - 无圈子时返回空榜单")
    void shouldReturnEmptyRankingWhenNoCircles() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("circle:ranking:hot")).thenReturn(null);
        when(circleMapper.selectHotCircles(100)).thenReturn(Collections.emptyList());

        // When
        CircleRankingVO result = rankingService.getHotRanking(20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("HOT");
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    @DisplayName("getNewRanking - 返回新增榜单")
    void shouldReturnNewRanking() {
        // Given
        Circle newCircle = new Circle();
        newCircle.setId("new-circle-1");
        newCircle.setName("新圈子");
        newCircle.setMemberCount(10);
        newCircle.setCategory("生活");
        newCircle.setDescription("新创建的圈子");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("circle:ranking:new")).thenReturn(null);
        when(circleMapper.selectNewCircles(100)).thenReturn(Collections.singletonList(newCircle));

        // When
        CircleRankingVO result = rankingService.getNewRanking(20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("NEW");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getCircleId()).isEqualTo("new-circle-1");
    }
}
