package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "第三方登录请求")
public class ContentAuthThirdPartyLoginReq {

    @NotBlank(message = "平台不能为空")
    @Schema(description = "第三方平台代码")
    private String provider;

    @NotBlank(message = "开放ID不能为空")
    @Schema(description = "第三方开放ID")
    private String openId;

    @Schema(description = "第三方联合ID")
    private String unionId;

    @Schema(description = "第三方昵称")
    private String nickname;

    @Schema(description = "第三方头像")
    private String avatar;

    @Schema(description = "原始授权JSON")
    private String rawJson;
}
