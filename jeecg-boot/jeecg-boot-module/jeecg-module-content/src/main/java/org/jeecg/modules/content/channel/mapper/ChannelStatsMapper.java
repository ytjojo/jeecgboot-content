package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.channel.entity.ChannelStats;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ChannelStatsMapper extends BaseMapper<ChannelStats> {

    List<ChannelStats> selectTrendData(@Param("channelId") String channelId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("statType") String statType);
}
