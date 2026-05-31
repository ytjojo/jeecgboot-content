package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ContentChannelRecommendationCache;
import org.jeecg.modules.content.channel.req.query.ChannelRecommendationQueryReq;
import org.jeecg.modules.content.channel.vo.ChannelRecommendationVO;

public interface IContentChannelRecommendationService extends IService<ContentChannelRecommendationCache> {

    IPage<ChannelRecommendationVO> getRecommendations(String userId, ChannelRecommendationQueryReq req);

    void markNotInterested(String userId, String channelId);

    IPage<ChannelRecommendationVO> getColdStartRecommendations(ChannelRecommendationQueryReq req);
}
