package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq;
import org.jeecg.modules.content.channel.req.query.ChannelRecommendationQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelEditorialPickService;
import org.jeecg.modules.content.channel.service.IContentChannelRankingService;
import org.jeecg.modules.content.channel.service.IContentChannelRecommendationService;
import org.jeecg.modules.content.channel.vo.ChannelEditorialPickVO;
import org.jeecg.modules.content.channel.vo.ChannelRankingItemVO;
import org.jeecg.modules.content.channel.vo.ChannelRecommendationVO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContentChannelDiscoveryBiz {

    @Resource
    private IContentChannelRecommendationService recommendationService;

    @Resource
    private IContentChannelRankingService rankingService;

    @Resource
    private IContentChannelEditorialPickService editorialPickService;

    public Map<String, Object> getDiscoveryData(String userId) {
        Map<String, Object> data = new HashMap<>();

        ChannelRecommendationQueryReq req = new ChannelRecommendationQueryReq();
        req.setPageNo(1);
        req.setPageSize(10);
        IPage<ChannelRecommendationVO> recommendations = recommendationService.getRecommendations(userId, req);
        data.put("recommendations", recommendations.getRecords());

        ChannelRankingQueryReq rankingReq = new ChannelRankingQueryReq();
        rankingReq.setDimension("DAILY");
        List<ChannelRankingItemVO> hotRanking = rankingService.getHotRanking(rankingReq);
        data.put("hotRanking", hotRanking);

        List<ChannelEditorialPickVO> editorialPicks = editorialPickService.listActivePicks();
        data.put("editorialPicks", editorialPicks);

        return data;
    }
}
