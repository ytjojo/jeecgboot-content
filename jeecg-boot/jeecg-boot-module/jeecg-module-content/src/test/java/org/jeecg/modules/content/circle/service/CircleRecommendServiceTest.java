package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleRecommendSource;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.jeecg.modules.content.circle.mapper.CircleRecommendSourceMapper;
import org.jeecg.modules.content.circle.service.impl.CircleRecommendServiceImpl;
import org.jeecg.modules.content.circle.vo.CircleRecommendVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleRecommendService 测试")
class CircleRecommendServiceTest {

    @Mock
    private CircleMapper circleMapper;

    @Mock
    private CircleMemberMapper memberMapper;

    @Mock
    private CircleRecommendSourceMapper sourceMapper;

    @InjectMocks
    private CircleRecommendServiceImpl recommendService;

    @Test
    @DisplayName("getRecommendations - 已加入圈子用户推荐")
    void shouldRecommendBasedOnUserInterests() {
        // Given
        String userId = "user-1";
        int limit = 10;

        // 用户已加入的圈子
        when(memberMapper.selectCircleIdsByUserId(userId)).thenReturn(Collections.singletonList("circle-1"));

        // 推荐候选圈子
        Circle circle1 = new Circle();
        circle1.setId("circle-2");
        circle1.setName("技术圈");
        circle1.setCategory("技术");
        circle1.setMemberCount(100);
        circle1.setPrivacyType(Circle.PrivacyType.PUBLIC);

        Circle circle2 = new Circle();
        circle2.setId("circle-3");
        circle2.setName("设计圈");
        circle2.setCategory("设计");
        circle2.setMemberCount(50);
        circle2.setPrivacyType(Circle.PrivacyType.PUBLIC);

        when(circleMapper.selectRecommendCandidates(anyString(), anyInt())).thenReturn(Arrays.asList(circle1, circle2));
        when(sourceMapper.insertBatch(anyList())).thenReturn(1);

        // When
        CircleRecommendVO result = recommendService.getRecommendations(userId, limit);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getCircleId()).isEqualTo("circle-2");
        assertThat(result.getItems().get(0).getSourceId()).isNotNull();
    }

    @Test
    @DisplayName("getRecommendations - 新用户返回热门榜单")
    void shouldReturnHotListForNewUser() {
        // Given
        String userId = "new-user";
        int limit = 10;

        when(memberMapper.selectCircleIdsByUserId(userId)).thenReturn(Collections.emptyList());

        // 热门圈子
        Circle hotCircle = new Circle();
        hotCircle.setId("hot-circle-1");
        hotCircle.setName("热门圈");
        hotCircle.setMemberCount(1000);
        hotCircle.setPrivacyType(Circle.PrivacyType.PUBLIC);

        when(circleMapper.selectHotCircles(anyInt())).thenReturn(Collections.singletonList(hotCircle));
        when(sourceMapper.insertBatch(anyList())).thenReturn(1);

        // When
        CircleRecommendVO result = recommendService.getRecommendations(userId, limit);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getCircleId()).isEqualTo("hot-circle-1");
    }
}
