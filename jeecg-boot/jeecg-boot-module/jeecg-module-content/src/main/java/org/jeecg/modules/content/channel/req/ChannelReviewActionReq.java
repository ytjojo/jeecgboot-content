package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "频道审核操作请求")
public class ChannelReviewActionReq {

    @Schema(description = "审核ID", required = true)
    private String reviewId;

    @Schema(description = "操作：approved/rejected/returned", required = true)
    private String action;

    @Schema(description = "审核原因")
    private String reason;
}
