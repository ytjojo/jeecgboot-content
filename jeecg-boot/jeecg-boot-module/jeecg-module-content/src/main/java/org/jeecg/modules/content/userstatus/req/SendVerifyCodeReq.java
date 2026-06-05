package org.jeecg.modules.content.userstatus.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送安全核验验证码请求。
 */
@Data
@Schema(description = "发送安全核验验证码请求")
public class SendVerifyCodeReq {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    private String phone;
}
