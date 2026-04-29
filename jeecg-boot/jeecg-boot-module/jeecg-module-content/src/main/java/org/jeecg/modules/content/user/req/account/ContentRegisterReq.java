package org.jeecg.modules.content.user.req.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Request model for content register.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区注册请求")
public class ContentRegisterReq {

    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邀请码")
    private String inviteCode;
}
