package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 绑定手机号请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "绑定手机号请求")
public class ContentAuthBindMobileReq {

    @Schema(description = "用户ID（由认证上下文自动填充）")
    private String userId;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "短信验证码")
    private String code;
}
