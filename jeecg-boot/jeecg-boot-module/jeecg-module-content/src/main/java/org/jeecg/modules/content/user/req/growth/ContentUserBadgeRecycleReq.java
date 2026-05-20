package org.jeecg.modules.content.user.req.growth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区勋章回收请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区勋章回收请求")
public class ContentUserBadgeRecycleReq {

    @NotBlank(message = "勋章授予ID不能为空")
    @Size(max = 64, message = "勋章授予ID长度不能超过64位")
    @Schema(description = "勋章授予ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String grantId;

    @NotBlank(message = "操作人ID不能为空")
    @Size(max = 64, message = "操作人ID长度不能超过64位")
    @Schema(description = "操作人ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String operatorUserId;

    @NotBlank(message = "回收原因不能为空")
    @Size(max = 255, message = "回收原因长度不能超过255位")
    @Schema(description = "回收原因", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String reason;
}
