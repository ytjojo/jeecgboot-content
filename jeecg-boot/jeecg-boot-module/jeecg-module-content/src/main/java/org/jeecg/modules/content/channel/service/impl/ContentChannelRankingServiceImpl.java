package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ContentChannelRankingSnapshot;
import org.jeecg.modules.content.channel.mapper.ContentChannelRankingSnapshotMapper;
import org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelRankingService;
import org.jeecg.modules.content.channel.vo.ChannelRankingItemVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentChannelRankingServiceImpl
        extends ServiceImpl<ContentChannelRankingSnapshotMapper, ContentChannelRankingSnapshot>
        implements IContentChannelRankingService {

    @Override
    public List<ChannelRankingItemVO> getHotRanking(ChannelRankingQueryReq req) {
        return getRanking("HOT", req.getDimension());
    }

    @Override
    public List<ChannelRankingItemVO> getNewRanking(ChannelRankingQueryReq req) {
        return getRanking("NEW", req.getDimension());
    }

    @Override
    public List<ChannelRankingItemVO> getSystemRanking(ChannelRankingQueryReq req) {
        return getRanking("SYSTEM", req.getDimension());
    }

    private List<ChannelRankingItemVO> getRanking(String type, String dimension) {
        List<ContentChannelRankingSnapshot> snapshots = list(
                Wrappers.<ContentChannelRankingSnapshot>lambdaQuery()
                        .eq(ContentChannelRankingSnapshot::getRankingType, type)
                        .eq(ContentChannelRankingSnapshot::getDimension, dimension)
                        .orderByAsc(ContentChannelRankingSnapshot::getRankPosition));

        return snapshots.stream().map(s -> {
            ChannelRankingItemVO vo = new ChannelRankingItemVO();
            vo.setChannelId(s.getChannelId());
            vo.setRankPosition(s.getRankPosition());
            vo.setScore(s.getScore());
            vo.setSnapshotDate(s.getSnapshotDate());
            return vo;
        }).collect(Collectors.toList());
    }
}
