package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 短信验证码登录请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "短信验证码登录请求")
public class ContentAuthSmsLoginReq {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "短信验证码")
    private String code;

    @Schema(description = "设备指纹")
    private String deviceFingerprint;
}
