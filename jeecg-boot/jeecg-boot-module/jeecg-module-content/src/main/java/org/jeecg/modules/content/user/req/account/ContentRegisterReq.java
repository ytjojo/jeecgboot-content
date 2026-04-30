package org.jeecg.modules.content.user.req.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Request model for content register.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区注册请求")
public class ContentRegisterReq {

    @Size(max = 64, message = "用户名长度不能超过64位")
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String username;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String mobile;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100位")
    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在6到32位之间")
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 20, message = "昵称长度不能超过20位")
    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String nickname;

    @Size(max = 32, message = "邀请码长度不能超过32位")
    @Schema(description = "邀请码", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String inviteCode;
}
