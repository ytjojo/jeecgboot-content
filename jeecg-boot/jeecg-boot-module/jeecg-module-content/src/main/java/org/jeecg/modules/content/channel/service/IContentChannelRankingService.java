package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ContentChannelRankingSnapshot;
import org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq;
import org.jeecg.modules.content.channel.vo.ChannelRankingItemVO;

import java.util.List;

public interface IContentChannelRankingService extends IService<ContentChannelRankingSnapshot> {

    List<ChannelRankingItemVO> getHotRanking(ChannelRankingQueryReq req);

    List<ChannelRankingItemVO> getNewRanking(ChannelRankingQueryReq req);

    List<ChannelRankingItemVO> getSystemRanking(ChannelRankingQueryReq req);
}
