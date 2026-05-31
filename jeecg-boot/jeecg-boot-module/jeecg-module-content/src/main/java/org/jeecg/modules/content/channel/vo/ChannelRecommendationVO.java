package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "推荐频道VO")
public class ChannelRecommendationVO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道名称")
    private String channelName;

    @Schema(description = "频道图标")
    private String channelIcon;

    @Schema(description = "频道类型")
    private String channelType;

    @Schema(description = "主分类名称")
    private String categoryName;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "订阅数")
    private Long subscriberCount;

    @Schema(description = "推荐理由")
    private String recommendationReason;

    @Schema(description = "推荐评分")
    private BigDecimal rankingScore;

    @Schema(description = "是否已订阅")
    private Boolean subscribed;
}
