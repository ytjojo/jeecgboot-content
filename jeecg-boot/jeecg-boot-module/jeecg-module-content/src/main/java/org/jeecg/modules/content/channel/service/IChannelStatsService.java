package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelStats;

import java.time.LocalDate;
import java.util.List;

public interface IChannelStatsService extends IService<ChannelStats> {

    ChannelStats getLatestStats(String channelId);

    List<ChannelStats> getTrendData(String channelId, LocalDate startDate, LocalDate endDate, String statType);
}
