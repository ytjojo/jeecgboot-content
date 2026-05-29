package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 换绑邮箱请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "换绑邮箱请求")
public class ContentAuthRebindEmailReq {

    @Schema(description = "用户ID（由认证上下文自动填充）")
    private String userId;

    @NotBlank(message = "旧邮箱验证码不能为空")
    @Schema(description = "旧邮箱验证码")
    private String oldCode;

    @NotBlank(message = "新邮箱不能为空")
    @Email(message = "新邮箱格式不正确")
    @Schema(description = "新邮箱")
    private String newEmail;

    @NotBlank(message = "新邮箱验证码不能为空")
    @Schema(description = "新邮箱验证码")
    private String newCode;
}
