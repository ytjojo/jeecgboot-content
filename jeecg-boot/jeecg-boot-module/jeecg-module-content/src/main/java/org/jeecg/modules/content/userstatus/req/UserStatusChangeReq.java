package org.jeecg.modules.content.userstatus.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;

import java.util.Date;

/**
 * 状态变更请求。
 */
@Data
@Schema(description = "状态变更请求")
public class UserStatusChangeReq {

    @NotNull(message = "目标状态不能为空")
    @Schema(description = "目标状态")
    private UserStatusEnum toStatus;

    @NotBlank(message = "变更原因不能为空")
    @Schema(description = "变更原因")
    private String reason;

    @Schema(description = "状态结束时间（可选，为空表示永久）")
    private Date endTime;

    @Schema(description = "备注")
    private String remark;
}
