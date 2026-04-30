package org.jeecg.modules.content.user.req.support;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Request model for content appeal create.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区申诉创建请求")
public class ContentAppealCreateReq {

    @NotBlank(message = "用户ID不能为空")
    @Size(max = 64, message = "用户ID长度不能超过64位")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String userId;

    @NotBlank(message = "申诉类型不能为空")
    @Size(max = 32, message = "申诉类型长度不能超过32位")
    @Schema(description = "申诉类型", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String appealType;

    @NotBlank(message = "申诉目标ID不能为空")
    @Size(max = 64, message = "申诉目标ID长度不能超过64位")
    @Schema(description = "申诉目标ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String targetId;

    @NotBlank(message = "申诉目标类型不能为空")
    @Size(max = 32, message = "申诉目标类型长度不能超过32位")
    @Schema(description = "申诉目标类型", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String targetType;

    @NotBlank(message = "申诉原因不能为空")
    @Size(max = 500, message = "申诉原因长度不能超过500位")
    @Schema(description = "申诉原因", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String reason;

    @Size(max = 2000, message = "申诉证据长度不能超过2000位")
    @Schema(description = "申诉证据JSON", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String evidenceJson;
}
