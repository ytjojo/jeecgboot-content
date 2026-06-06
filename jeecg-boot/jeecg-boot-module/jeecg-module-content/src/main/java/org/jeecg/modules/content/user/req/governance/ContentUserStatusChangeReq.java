package org.jeecg.modules.content.user.req.governance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Request model for content user status change.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户状态变更请求")
public class ContentUserStatusChangeReq {

    @NotBlank(message = "用户ID不能为空")
    @Size(max = 64, message = "用户ID长度不能超过64位")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String userId;

    @NotBlank(message = "当前状态不能为空")
    @Pattern(
        regexp = "^(GUEST|REGISTERED_INCOMPLETE|NORMAL|MUTED|RESTRICTED_RECOMMEND|FROZEN|BANNED|DEACTIVATING|DEACTIVATED)$",
        message = "当前状态取值不合法"
    )
    @Schema(description = "当前状态", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String currentStatus;

    @NotBlank(message = "目标状态不能为空")
    @Pattern(
        regexp = "^(GUEST|REGISTERED_INCOMPLETE|NORMAL|MUTED|RESTRICTED_RECOMMEND|FROZEN|BANNED|DEACTIVATING|DEACTIVATED)$",
        message = "目标状态取值不合法"
    )
    @Schema(description = "目标状态", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String targetStatus;

    @NotBlank(message = "操作人不能为空")
    @Size(max = 64, message = "操作人长度不能超过64位")
    @Schema(description = "操作人用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String operatorUserId;

    @Size(max = 500, message = "变更原因长度不能超过500位")
    @Schema(description = "变更原因", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String reason;

    @Size(max = 64, message = "规则编码长度不能超过64位")
    @Schema(description = "规则编码", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String ruleCode;

    @Schema(description = "生效开始时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Date effectiveStartTime;

    @Schema(description = "生效结束时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Date effectiveEndTime;
}
