package org.jeecg.modules.content.channel.req.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "审核请求")
public class ChannelReviewReq {

    @NotBlank(message = "审核记录ID不能为空")
    @Schema(description = "审核记录ID")
    private String reviewId;

    @NotBlank(message = "审核动作不能为空")
    @Schema(description = "审核动作：APPROVE/REJECT")
    private String action;

    @Schema(description = "拒绝原因（拒绝时必填）")
    private String rejectReason;
}
