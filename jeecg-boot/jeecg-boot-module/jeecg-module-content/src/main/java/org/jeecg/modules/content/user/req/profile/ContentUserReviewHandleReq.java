package org.jeecg.modules.content.user.req.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 资料审核处理请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区资料审核处理请求")
public class ContentUserReviewHandleReq {

    @NotBlank(message = "审核ID不能为空")
    @Schema(description = "审核ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reviewId;

    @NotBlank(message = "审核结果不能为空")
    @Schema(description = "审核结果：APPROVED 或 REJECTED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reviewStatus;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "审核人")
    private String operatorUserId;
}
