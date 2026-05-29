package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 换绑手机号请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "换绑手机号请求")
public class ContentAuthRebindMobileReq {

    @Schema(description = "用户ID（由认证上下文自动填充）")
    private String userId;

    @NotBlank(message = "旧手机号验证码不能为空")
    @Schema(description = "旧手机号验证码")
    private String oldCode;

    @NotBlank(message = "新手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "新手机号格式不正确")
    @Schema(description = "新手机号")
    private String newMobile;

    @NotBlank(message = "新手机号验证码不能为空")
    @Schema(description = "新手机号验证码")
    private String newCode;
}
