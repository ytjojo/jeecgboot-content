package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 通用密码重置请求（支持手机号/邮箱重置）。
 */
@Data
@Accessors(chain = true)
@Schema(description = "密码重置请求")
public class ContentAuthPasswordResetReq {

    @Schema(description = "用户ID（由认证上下文自动填充）")
    private String userId;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度8-32位")
    @Schema(description = "新密码")
    private String newPassword;

    @NotBlank(message = "重置类型不能为空")
    @Schema(description = "重置类型: MOBILE/EMAIL")
    private String resetType;

    @Schema(description = "重置目标(手机号或邮箱)")
    private String target;
}
