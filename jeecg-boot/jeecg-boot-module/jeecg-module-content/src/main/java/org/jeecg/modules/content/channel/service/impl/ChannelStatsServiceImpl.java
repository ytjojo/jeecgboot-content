package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelStats;
import org.jeecg.modules.content.channel.mapper.ChannelStatsMapper;
import org.jeecg.modules.content.channel.service.IChannelStatsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ChannelStatsServiceImpl extends ServiceImpl<ChannelStatsMapper, ChannelStats>
    implements IChannelStatsService {

    @Override
    public ChannelStats getLatestStats(String channelId) {
        return baseMapper.selectOne(
            new LambdaQueryWrapper<ChannelStats>()
                .eq(ChannelStats::getChannelId, channelId)
                .orderByDesc(ChannelStats::getStatDate)
                .last("LIMIT 1")
        );
    }

    @Override
    public List<ChannelStats> getTrendData(String channelId, LocalDate startDate, LocalDate endDate, String statType) {
        return baseMapper.selectTrendData(channelId, startDate, endDate, statType);
    }
}
