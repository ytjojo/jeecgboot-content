package org.jeecg.modules.content.circle.req.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "加入圈子请求")
public class CircleJoinReq {

    @NotBlank(message = "圈子ID不能为空")
    @Schema(description = "圈子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String circleId;

    @Schema(description = "密码(当圈子为密码保护时必填)")
    private String password;
}
