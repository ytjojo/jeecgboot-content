package org.jeecg.modules.content.user.req.support;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Request model for content report handling.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区举报处理请求")
public class ContentReportHandleReq {

    @NotBlank(message = "举报ID不能为空")
    @Size(max = 64, message = "举报ID长度不能超过64位")
    @Schema(description = "举报ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String reportId;

    @NotBlank(message = "处理人ID不能为空")
    @Size(max = 64, message = "处理人ID长度不能超过64位")
    @Schema(description = "处理人用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String operatorUserId;

    @NotBlank(message = "处理后状态不能为空")
    @Pattern(regexp = "^RESOLVED$", message = "处理后状态仅支持RESOLVED")
    @Schema(description = "处理后状态", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String status;

    @NotBlank(message = "处理结果状态不能为空")
    @Size(max = 32, message = "处理结果状态长度不能超过32位")
    @Schema(description = "处理结果状态", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String resultStatus;

    @NotBlank(message = "处理结果说明不能为空")
    @Size(max = 500, message = "处理结果说明长度不能超过500位")
    @Schema(description = "处理结果说明", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String resultNote;

    @NotBlank(message = "处理进度说明不能为空")
    @Size(max = 500, message = "处理进度说明长度不能超过500位")
    @Schema(description = "处理进度说明", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String progressNote;
}
