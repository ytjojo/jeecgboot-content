package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.entity.ChannelStats;
import org.jeecg.modules.content.channel.service.IChannelStatsService;
import org.jeecg.modules.content.channel.vo.ChannelStatsVO;
import org.jeecg.modules.content.channel.vo.ChannelTrendVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChannelStatsBiz {

    @Resource
    private IChannelStatsService statsService;

    public ChannelStatsVO getCoreStats(String channelId) {
        ChannelStats stats = statsService.getLatestStats(channelId);
        if (stats == null) {
            return ChannelStatsVO.builder()
                    .channelId(channelId)
                    .subscriberCount(0)
                    .contentCount(0)
                    .pv(0L)
                    .uv(0L)
                    .likeCount(0L)
                    .commentCount(0L)
                    .favoriteCount(0L)
                    .shareCount(0L)
                    .effectiveVisitCount(0L)
                    .build();
        }
        return ChannelStatsVO.builder()
                .channelId(stats.getChannelId())
                .subscriberCount(stats.getSubscriberCount())
                .contentCount(stats.getContentCount())
                .pv(stats.getPv())
                .uv(stats.getUv())
                .likeCount(stats.getLikeCount())
                .commentCount(stats.getCommentCount())
                .favoriteCount(stats.getFavoriteCount())
                .shareCount(stats.getShareCount())
                .effectiveVisitCount(stats.getEffectiveVisitCount())
                .dataUpdateTime(stats.getUpdatedTime())
                .build();
    }

    public ChannelTrendVO getTrendData(String channelId, LocalDate startDate, LocalDate endDate, String statType) {
        List<ChannelStats> trendList = statsService.getTrendData(channelId, startDate, endDate, statType);
        return ChannelTrendVO.builder()
                .dates(trendList.stream().map(ChannelStats::getStatDate).collect(Collectors.toList()))
                .subscriberCounts(trendList.stream().map(ChannelStats::getSubscriberCount).collect(Collectors.toList()))
                .contentCounts(trendList.stream().map(ChannelStats::getContentCount).collect(Collectors.toList()))
                .pvs(trendList.stream().map(ChannelStats::getPv).collect(Collectors.toList()))
                .uvs(trendList.stream().map(ChannelStats::getUv).collect(Collectors.toList()))
                .build();
    }
}
