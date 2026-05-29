package org.jeecg.modules.content.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 登录结果。
 */
@Data
@Accessors(chain = true)
@Schema(description = "登录结果")
public class AuthLoginResult {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "访问token")
    private String accessToken;

    @Schema(description = "token类型")
    private String tokenType = "Bearer";

    @Schema(description = "Token JTI")
    private String jti;
}
