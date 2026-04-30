package org.jeecg.modules.content.user.req.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.common.util.oConvertUtils;

/**
 * Request model for content password reset.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区密码重置请求")
public class ContentPasswordResetReq {

    @Schema(description = "用户ID")
    private String userId;

    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String mobile;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度需在6到32位之间")
    @Schema(description = "新密码")
    private String newPassword;

    /**
     * 校验用户ID、手机号、邮箱至少提供一项用于定位账号。
     */
    @AssertTrue(message = "用户ID、手机号、邮箱至少填写一项")
    public boolean isIdentityProvided() {
        return oConvertUtils.isNotEmpty(userId)
            || oConvertUtils.isNotEmpty(mobile)
            || oConvertUtils.isNotEmpty(email);
    }
}
