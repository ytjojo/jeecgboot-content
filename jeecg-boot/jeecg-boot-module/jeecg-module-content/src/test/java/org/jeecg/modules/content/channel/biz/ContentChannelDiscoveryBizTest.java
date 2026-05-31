package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.channel.entity.ContentChannelRankingSnapshot;
import org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq;
import org.jeecg.modules.content.channel.req.query.ChannelRecommendationQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelEditorialPickService;
import org.jeecg.modules.content.channel.service.IContentChannelRankingService;
import org.jeecg.modules.content.channel.service.IContentChannelRecommendationService;
import org.jeecg.modules.content.channel.vo.ChannelEditorialPickVO;
import org.jeecg.modules.content.channel.vo.ChannelRankingItemVO;
import org.jeecg.modules.content.channel.vo.ChannelRecommendationVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentChannelDiscoveryBizTest {

    @Mock
    private IContentChannelRecommendationService recommendationService;

    @Mock
    private IContentChannelRankingService rankingService;

    @Mock
    private IContentChannelEditorialPickService editorialPickService;

    @InjectMocks
    private ContentChannelDiscoveryBiz discoveryBiz;

    @Test
    void getDiscoveryData_shouldReturnAllSections() {
        // 推荐
        Page<ChannelRecommendationVO> recPage = new Page<>(1, 10);
        ChannelRecommendationVO recVo = new ChannelRecommendationVO();
        recVo.setChannelId("ch1");
        recVo.setRecommendationReason("测试推荐");
        recPage.setRecords(Arrays.asList(recVo));
        when(recommendationService.getRecommendations(anyString(), any(ChannelRecommendationQueryReq.class)))
                .thenReturn(recPage);

        // 排行榜
        ChannelRankingItemVO rankVo = new ChannelRankingItemVO();
        rankVo.setChannelId("ch2");
        rankVo.setRankPosition(1);
        rankVo.setScore(new BigDecimal("95.0000"));
        rankVo.setSnapshotDate(new Date());
        when(rankingService.getHotRanking(any(ChannelRankingQueryReq.class)))
                .thenReturn(Arrays.asList(rankVo));

        // 编辑精选
        ChannelEditorialPickVO pickVo = new ChannelEditorialPickVO();
        pickVo.setId("pick1");
        pickVo.setChannelId("ch3");
        pickVo.setRecommendationText("精选推荐");
        when(editorialPickService.listActivePicks())
                .thenReturn(Arrays.asList(pickVo));

        Map<String, Object> result = discoveryBiz.getDiscoveryData("user1");

        assertThat(result).containsKeys("recommendations", "hotRanking", "editorialPicks");
        assertThat(((java.util.List<?>) result.get("recommendations"))).hasSize(1);
        assertThat(((java.util.List<?>) result.get("hotRanking"))).hasSize(1);
        assertThat(((java.util.List<?>) result.get("editorialPicks"))).hasSize(1);
    }
}
