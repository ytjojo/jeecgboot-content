package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.constant.ChannelStatsConstant;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelStats;
import org.jeecg.modules.content.channel.entity.ChannelSubscription;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.service.ChannelSubscriptionService;
import org.jeecg.modules.content.channel.service.IChannelStatsService;
import org.jeecg.modules.content.channel.vo.ChannelHotContentVO;
import org.jeecg.modules.content.channel.vo.ChannelInteractionStatsVO;
import org.jeecg.modules.content.channel.vo.ChannelStatsVO;
import org.jeecg.modules.content.channel.vo.ChannelTrendVO;
import org.jeecg.modules.content.channel.vo.ChannelUserAnalysisVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ChannelStatsBiz {

    @Resource
    private IChannelStatsService statsService;

    @Resource
    private ChannelSubscriptionService subscriptionService;

    @Resource
    private ChannelContentPublishMapper publishMapper;

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

    public List<ChannelHotContentVO> getHotContent(String channelId, Integer limit, Integer days) {
        int actualLimit = (limit != null) ? limit : ChannelStatsConstant.HOT_CONTENT_DEFAULT_LIMIT;
        int actualDays = (days != null) ? days : ChannelStatsConstant.HOT_CONTENT_DEFAULT_DAYS;

        Date sinceDate = Date.from(LocalDateTime.now().minusDays(actualDays)
                .atZone(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<ChannelContentPublish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelContentPublish::getChannelId, channelId)
                .eq(ChannelContentPublish::getPublishStatus, "PUBLISHED")
                .ge(ChannelContentPublish::getCreateTime, sinceDate)
                .orderByDesc(ChannelContentPublish::getCreateTime)
                .last("LIMIT " + actualLimit);

        List<ChannelContentPublish> contentList = publishMapper.selectList(wrapper);

        AtomicInteger rank = new AtomicInteger(1);
        List<ChannelHotContentVO> result = new ArrayList<>();
        for (ChannelContentPublish c : contentList) {
            LocalDateTime publishTime = c.getCreateTime() != null
                    ? c.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    : null;
            result.add(ChannelHotContentVO.builder()
                    .contentId(c.getContentId())
                    .title(c.getContentId())
                    .contentType(c.getContentType())
                    .publishTime(publishTime)
                    .effectiveInteractionCount(0L)
                    .rank(rank.getAndIncrement())
                    .build());
        }
        return result;
    }

    public ChannelInteractionStatsVO getInteractionStats(String channelId) {
        ChannelStats stats = statsService.getLatestStats(channelId);
        if (stats == null) {
            return ChannelInteractionStatsVO.builder()
                    .channelId(channelId)
                    .likeCount(0L)
                    .commentCount(0L)
                    .favoriteCount(0L)
                    .shareCount(0L)
                    .build();
        }
        return ChannelInteractionStatsVO.builder()
                .channelId(stats.getChannelId())
                .likeCount(stats.getLikeCount())
                .commentCount(stats.getCommentCount())
                .favoriteCount(stats.getFavoriteCount())
                .shareCount(stats.getShareCount())
                .build();
    }

    public ChannelUserAnalysisVO getUserAnalysis(String channelId, LocalDate startDate, LocalDate endDate) {
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<ChannelSubscription> subWrapper = new LambdaQueryWrapper<>();
        subWrapper.eq(ChannelSubscription::getChannelId, channelId)
                .ge(ChannelSubscription::getCreateTime, start)
                .le(ChannelSubscription::getCreateTime, end);
        long newCount = subscriptionService.count(subWrapper);

        return ChannelUserAnalysisVO.builder()
                .newSubscriberCount((int) newCount)
                .lostSubscriberCount(0)
                .activityDistribution(Collections.emptyMap())
                .contributionRanking(Collections.emptyList())
                .build();
    }
}
