package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 绑定第三方账号请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "绑定第三方账号请求")
public class ContentAuthBindThirdPartyReq {

    @Schema(description = "用户ID（由认证上下文自动填充）")
    private String userId;

    @NotBlank(message = "第三方平台不能为空")
    @Schema(description = "第三方平台")
    private String provider;

    @NotBlank(message = "第三方开放ID不能为空")
    @Schema(description = "第三方开放ID")
    private String openId;

    @Schema(description = "第三方联合ID")
    private String unionId;
}
