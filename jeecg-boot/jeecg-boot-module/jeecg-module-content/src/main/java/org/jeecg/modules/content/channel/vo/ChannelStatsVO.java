package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "频道核心指标VO")
public class ChannelStatsVO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "订阅数")
    private Integer subscriberCount;

    @Schema(description = "内容数")
    private Integer contentCount;

    @Schema(description = "浏览量")
    private Long pv;

    @Schema(description = "访客数")
    private Long uv;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "评论数")
    private Long commentCount;

    @Schema(description = "收藏数")
    private Long favoriteCount;

    @Schema(description = "分享数")
    private Long shareCount;

    @Schema(description = "有效访问数")
    private Long effectiveVisitCount;

    @Schema(description = "数据更新时间")
    private LocalDateTime dataUpdateTime;
}
