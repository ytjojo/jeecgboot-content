package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 邮箱重置密码请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "邮箱重置密码请求")
public class ContentAuthResetPasswordByEmailReq {

    @NotBlank(message = "重置token不能为空")
    @Schema(description = "邮箱重置token")
    private String token;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需在8到32位之间")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    @Schema(description = "新密码")
    private String newPassword;
}
