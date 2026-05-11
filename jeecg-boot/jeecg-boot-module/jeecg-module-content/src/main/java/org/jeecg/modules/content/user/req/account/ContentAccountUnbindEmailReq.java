package org.jeecg.modules.content.user.req.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区解绑邮箱请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区解绑邮箱请求")
public class ContentAccountUnbindEmailReq {

    @NotBlank(message = "用户ID不能为空")
    @Size(max = 64, message = "用户ID长度不能超过64位")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String userId;

    @Size(max = 64, message = "操作人ID长度不能超过64位")
    @Schema(description = "操作人ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String operatorUserId;

    @Schema(description = "是否已完成二次校验", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean secondaryVerified;
}
