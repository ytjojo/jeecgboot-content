package org.jeecg.modules.content.circle.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 圈子加入审核请求。
 */
@Data
@Schema(description = "圈子加入审核请求")
public class CircleJoinReviewReq {

    @NotBlank(message = "申请ID不能为空")
    @Schema(description = "申请ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String requestId;

    @Schema(description = "拒绝原因（拒绝时填写）")
    private String rejectReason;
}
