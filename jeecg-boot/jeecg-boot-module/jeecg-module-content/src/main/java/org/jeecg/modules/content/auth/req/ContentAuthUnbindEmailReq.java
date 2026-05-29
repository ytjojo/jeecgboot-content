package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 解绑邮箱请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "解绑邮箱请求")
public class ContentAuthUnbindEmailReq {

    @Schema(description = "用户ID（由认证上下文自动填充）")
    private String userId;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码")
    private String code;
}
