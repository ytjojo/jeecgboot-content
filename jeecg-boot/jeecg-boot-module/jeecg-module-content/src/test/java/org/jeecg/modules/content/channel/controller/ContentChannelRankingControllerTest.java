package org.jeecg.modules.content.channel.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelRankingService;
import org.jeecg.modules.content.channel.vo.ChannelRankingItemVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道排行榜控制器测试
 */
@ExtendWith(MockitoExtension.class)
class ContentChannelRankingControllerTest {

    @Mock
    private IContentChannelRankingService rankingService;

    @InjectMocks
    private ContentChannelRankingController controller;

    @Test
    void should_get_hot_ranking() {
        ChannelRankingQueryReq req = new ChannelRankingQueryReq();
        when(rankingService.getHotRanking(req)).thenReturn(List.of());

        Result<List<ChannelRankingItemVO>> result = controller.getHotRanking(req);

        assertThat(result.isSuccess()).isTrue();
        verify(rankingService).getHotRanking(req);
    }

    @Test
    void should_get_new_ranking() {
        ChannelRankingQueryReq req = new ChannelRankingQueryReq();
        when(rankingService.getNewRanking(req)).thenReturn(List.of());

        Result<List<ChannelRankingItemVO>> result = controller.getNewRanking(req);

        assertThat(result.isSuccess()).isTrue();
        verify(rankingService).getNewRanking(req);
    }

    @Test
    void should_get_system_ranking() {
        ChannelRankingQueryReq req = new ChannelRankingQueryReq();
        when(rankingService.getSystemRanking(req)).thenReturn(List.of());

        Result<List<ChannelRankingItemVO>> result = controller.getSystemRanking(req);

        assertThat(result.isSuccess()).isTrue();
        verify(rankingService).getSystemRanking(req);
    }
}
