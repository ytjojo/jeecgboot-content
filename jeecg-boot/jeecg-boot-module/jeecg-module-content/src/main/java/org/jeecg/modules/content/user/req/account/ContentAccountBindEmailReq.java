package org.jeecg.modules.content.user.req.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区绑定邮箱请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区绑定邮箱请求")
public class ContentAccountBindEmailReq {

    @NotBlank(message = "用户ID不能为空")
    @Size(max = 64, message = "用户ID长度不能超过64位")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String userId;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100位")
    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String email;

    @Size(max = 64, message = "操作人ID长度不能超过64位")
    @Schema(description = "操作人ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String operatorUserId;

    @Schema(description = "是否已完成二次校验", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean secondaryVerified;
}
