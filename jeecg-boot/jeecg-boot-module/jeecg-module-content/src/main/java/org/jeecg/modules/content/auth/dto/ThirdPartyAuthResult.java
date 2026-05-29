package org.jeecg.modules.content.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 第三方登录结果。
 */
@Data
@Accessors(chain = true)
@Schema(description = "第三方登录结果")
public class ThirdPartyAuthResult {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "是否为新注册用户")
    private boolean newUser;

    @Schema(description = "是否需要完善资料")
    private boolean profileIncomplete;
}
