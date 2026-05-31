package org.jeecg.modules.content.circle.req.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "圈子成员操作请求")
public class CircleMemberUpdateReq {

    @NotBlank(message = "圈子ID不能为空")
    @Schema(description = "圈子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String circleId;

    @NotBlank(message = "目标用户ID不能为空")
    @Schema(description = "目标用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetUserId;

    @Schema(description = "目标角色(角色变更时必填)")
    private String targetRole;

    @Schema(description = "禁言时长: 1h/24h/7d/PERMANENT")
    private String muteDuration;

    @Schema(description = "操作原因")
    private String reason;
}
