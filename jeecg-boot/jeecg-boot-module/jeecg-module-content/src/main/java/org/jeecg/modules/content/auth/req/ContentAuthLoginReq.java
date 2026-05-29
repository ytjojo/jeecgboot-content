package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 密码登录请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "密码登录请求")
public class ContentAuthLoginReq {

    @NotBlank(message = "登录标识不能为空")
    @Schema(description = "手机号或邮箱")
    private String identifier;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;

    @Schema(description = "设备指纹")
    private String deviceFingerprint;
}
