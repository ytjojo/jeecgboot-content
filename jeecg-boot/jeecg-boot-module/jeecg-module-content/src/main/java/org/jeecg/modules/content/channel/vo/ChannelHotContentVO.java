package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "热门内容响应")
public class ChannelHotContentVO {

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容标题")
    private String title;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "有效互动量")
    private Long effectiveInteractionCount;

    @Schema(description = "排名")
    private Integer rank;
}
