package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "排行榜条目VO")
public class ChannelRankingItemVO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道名称")
    private String channelName;

    @Schema(description = "频道图标")
    private String channelIcon;

    @Schema(description = "排名位置")
    private Integer rankPosition;

    @Schema(description = "综合得分")
    private BigDecimal score;

    @Schema(description = "快照日期")
    private Date snapshotDate;

    @Schema(description = "更新时间")
    private Date updateTime;
}
