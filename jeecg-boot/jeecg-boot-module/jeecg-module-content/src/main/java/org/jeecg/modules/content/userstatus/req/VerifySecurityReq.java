package org.jeecg.modules.content.userstatus.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 安全核验请求。
 */
@Data
@Schema(description = "安全核验请求")
public class VerifySecurityReq {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码")
    private String verifyCode;
}
