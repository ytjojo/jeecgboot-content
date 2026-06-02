package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.req.query.ChannelRecommendationQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelRecommendationService;
import org.jeecg.modules.content.channel.vo.ChannelRecommendationVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道推荐控制器测试
 */
@ExtendWith(MockitoExtension.class)
class ContentChannelRecommendationControllerTest {

    @Mock
    private IContentChannelRecommendationService recommendationService;

    @InjectMocks
    private ContentChannelRecommendationController controller;

    @Test
    void should_get_recommendations() {
        ChannelRecommendationQueryReq req = new ChannelRecommendationQueryReq();
        when(recommendationService.getRecommendations("user1", req)).thenReturn(null);

        Result<IPage<ChannelRecommendationVO>> result = controller.getRecommendations("user1", req);

        assertThat(result.isSuccess()).isTrue();
        verify(recommendationService).getRecommendations("user1", req);
    }

    @Test
    void should_get_cold_start_recommendations() {
        ChannelRecommendationQueryReq req = new ChannelRecommendationQueryReq();
        when(recommendationService.getColdStartRecommendations(req)).thenReturn(null);

        Result<IPage<ChannelRecommendationVO>> result = controller.getColdStartRecommendations(req);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void should_mark_not_interested() {
        Result<Void> result = controller.markNotInterested("user1", "ch1");

        assertThat(result.isSuccess()).isTrue();
        verify(recommendationService).markNotInterested("user1", "ch1");
    }
}
