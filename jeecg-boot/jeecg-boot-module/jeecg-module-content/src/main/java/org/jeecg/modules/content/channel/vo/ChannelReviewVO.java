package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "频道审核记录VO")
public class ChannelReviewVO {

    @Schema(description = "审核ID")
    private String reviewId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "审核类型")
    private String reviewType;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "申请人ID")
    private String applicantId;

    @Schema(description = "审核人ID")
    private String reviewerId;

    @Schema(description = "审核原因")
    private String reviewReason;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "是否超时")
    private Integer timeoutFlag;
}
