package org.jeecg.modules.content.user.req.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Request model for content password reset.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区密码重置请求")
public class ContentPasswordResetReq {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码")
    private String newPassword;
}
