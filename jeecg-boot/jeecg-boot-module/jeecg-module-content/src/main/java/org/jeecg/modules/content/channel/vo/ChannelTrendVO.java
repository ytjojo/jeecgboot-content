package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Schema(description = "频道趋势数据VO")
public class ChannelTrendVO {

    @Schema(description = "日期列表")
    private List<LocalDate> dates;

    @Schema(description = "订阅数趋势")
    private List<Integer> subscriberCounts;

    @Schema(description = "内容数趋势")
    private List<Integer> contentCounts;

    @Schema(description = "PV趋势")
    private List<Long> pvs;

    @Schema(description = "UV趋势")
    private List<Long> uvs;
}
