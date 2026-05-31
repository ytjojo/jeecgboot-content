package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "频道统计查询请求")
public class ChannelStatsReq {

    @Schema(description = "频道ID", required = true)
    private String channelId;

    @Schema(description = "统计类型：daily/weekly/monthly")
    private String statType;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;
}
