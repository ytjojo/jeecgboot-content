package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.channel.entity.ContentChannelNotInterested;
import org.jeecg.modules.content.channel.entity.ContentChannelRecommendationCache;
import org.jeecg.modules.content.channel.mapper.ContentChannelNotInterestedMapper;
import org.jeecg.modules.content.channel.mapper.ContentChannelRecommendationCacheMapper;
import org.jeecg.modules.content.channel.req.query.ChannelRecommendationQueryReq;
import org.jeecg.modules.content.channel.service.impl.ContentChannelRecommendationServiceImpl;
import org.jeecg.modules.content.channel.vo.ChannelRecommendationVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentChannelRecommendationServiceTest {

    @Mock
    private ContentChannelRecommendationCacheMapper recommendationCacheMapper;

    @Mock
    private ContentChannelNotInterestedMapper notInterestedMapper;

    @Mock
    private IContentChannelVisibilityService visibilityService;

    @InjectMocks
    private ContentChannelRecommendationServiceImpl recommendationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(recommendationService, "baseMapper", recommendationCacheMapper);
    }

    @Test
    void getRecommendations_shouldReturnCachedRecommendations() {
        ContentChannelRecommendationCache cache = new ContentChannelRecommendationCache();
        cache.setUserId("user1");
        cache.setChannelId("ch1");
        cache.setRankingScore(new BigDecimal("85.5000"));
        cache.setRecommendationRule("SIMILARITY");
        cache.setRecommendationReason("因为你订阅了相似频道");
        cache.setRecommendationStatus(1);

        Page<ContentChannelRecommendationCache> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(cache));
        page.setTotal(1);

        when(notInterestedMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());
        when(recommendationCacheMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        ChannelRecommendationQueryReq req = new ChannelRecommendationQueryReq();
        req.setPageNo(1);
        req.setPageSize(10);

        IPage<ChannelRecommendationVO> result = recommendationService.getRecommendations("user1", req);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getRecommendationReason()).isEqualTo("因为你订阅了相似频道");
    }

    @Test
    void markNotInterested_shouldCreateFeedback() {
        when(notInterestedMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(notInterestedMapper.insert(any(ContentChannelNotInterested.class))).thenReturn(1);

        recommendationService.markNotInterested("user1", "ch1");

        verify(notInterestedMapper).insert(any(ContentChannelNotInterested.class));
    }

    @Test
    void markNotInterested_shouldUpdateExistingFeedback() {
        // 使用 mock 对象避免 Lombok 编译问题
        ContentChannelNotInterested existing = mock(ContentChannelNotInterested.class);

        when(notInterestedMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(notInterestedMapper.updateById(any(ContentChannelNotInterested.class))).thenReturn(1);

        recommendationService.markNotInterested("user1", "ch1");

        verify(notInterestedMapper).updateById(any(ContentChannelNotInterested.class));
        verify(notInterestedMapper, never()).insert(any(ContentChannelNotInterested.class));
    }

    @Test
    void getColdStartRecommendations_shouldReturnPage() {
        ChannelRecommendationQueryReq req = new ChannelRecommendationQueryReq();
        req.setPageNo(1);
        req.setPageSize(10);

        IPage<ChannelRecommendationVO> result = recommendationService.getColdStartRecommendations(req);

        assertThat(result).isNotNull();
    }
}
