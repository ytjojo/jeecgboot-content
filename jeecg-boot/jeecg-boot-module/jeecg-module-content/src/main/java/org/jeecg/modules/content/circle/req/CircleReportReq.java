package org.jeecg.modules.content.circle.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 内容举报请求。
 */
@Data
@Schema(description = "内容举报请求")
public class CircleReportReq {

    @NotBlank(message = "圈子ID不能为空")
    @Schema(description = "圈子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String circleId;

    @NotBlank(message = "内容ID不能为空")
    @Schema(description = "被举报的内容ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contentId;

    @Schema(description = "举报原因")
    private String reason;
}
