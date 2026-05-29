package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 邮箱密码注册请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "邮箱密码注册请求")
public class ContentAuthEmailRegisterReq {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100位")
    @Schema(description = "邮箱")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需在8到32位之间")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    @Schema(description = "密码")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 20, message = "昵称长度不能超过20位")
    @Schema(description = "昵称")
    private String nickname;

    @Size(max = 32, message = "邀请码长度不能超过32位")
    @Schema(description = "邀请码")
    private String inviteCode;
}
