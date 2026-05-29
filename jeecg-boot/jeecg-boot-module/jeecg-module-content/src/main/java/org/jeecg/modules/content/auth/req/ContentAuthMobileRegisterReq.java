package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 手机号验证码注册请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "手机号验证码注册请求")
public class ContentAuthMobileRegisterReq {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码长度为6位")
    @Schema(description = "短信验证码")
    private String code;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 20, message = "昵称长度不能超过20位")
    @Schema(description = "昵称")
    private String nickname;

    @Size(max = 32, message = "邀请码长度不能超过32位")
    @Schema(description = "邀请码")
    private String inviteCode;
}
