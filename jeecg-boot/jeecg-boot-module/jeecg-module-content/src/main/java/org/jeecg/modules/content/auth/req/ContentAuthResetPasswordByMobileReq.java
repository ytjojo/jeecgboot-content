package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 手机号重置密码请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "手机号重置密码请求")
public class ContentAuthResetPasswordByMobileReq {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "短信验证码")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需在8到32位之间")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    @Schema(description = "新密码")
    private String newPassword;
}
