package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelStatsBiz;
import org.jeecg.modules.content.channel.constant.ChannelStatsConstant;
import org.jeecg.modules.content.channel.vo.ChannelStatsVO;
import org.jeecg.modules.content.channel.vo.ChannelTrendVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/content/channel/stats")
@Tag(name = "频道统计", description = "频道数据统计看板接口")
public class ChannelStatsController {

    @Resource
    private ChannelStatsBiz channelStatsBiz;

    @GetMapping("/core")
    @Operation(summary = "获取核心指标")
    public Result<ChannelStatsVO> getCoreStats(
            @Parameter(description = "频道ID", required = true)
            @RequestParam String channelId) {
        return Result.OK(channelStatsBiz.getCoreStats(channelId));
    }

    @GetMapping("/trend")
    @Operation(summary = "获取趋势数据")
    public Result<ChannelTrendVO> getTrendData(
            @Parameter(description = "频道ID", required = true)
            @RequestParam String channelId,
            @Parameter(description = "开始日期")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "统计类型：daily/weekly/monthly")
            @RequestParam(defaultValue = ChannelStatsConstant.STAT_TYPE_DAILY) String statType) {
        return Result.OK(channelStatsBiz.getTrendData(channelId, startDate, endDate, statType));
    }
}
