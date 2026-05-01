package org.jeecg.modules.content.user.req.support;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Request model for content report creation.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区举报创建请求")
public class ContentReportCreateReq {

    @NotBlank(message = "举报用户ID不能为空")
    @Size(max = 64, message = "举报用户ID长度不能超过64位")
    @Schema(description = "举报用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String userId;

    @NotBlank(message = "举报目标类型不能为空")
    @Size(max = 32, message = "举报目标类型长度不能超过32位")
    @Schema(description = "举报目标类型", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String targetType;

    @NotBlank(message = "举报目标ID不能为空")
    @Size(max = 64, message = "举报目标ID长度不能超过64位")
    @Schema(description = "举报目标ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String targetId;

    @NotBlank(message = "举报类型不能为空")
    @Size(max = 32, message = "举报类型长度不能超过32位")
    @Schema(description = "举报类型", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String reportType;

    @NotBlank(message = "举报原因不能为空")
    @Size(max = 500, message = "举报原因长度不能超过500位")
    @Schema(description = "举报原因", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String reason;

    @Size(max = 2000, message = "举报证据长度不能超过2000位")
    @Schema(description = "举报证据JSON", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String evidenceJson;
}
