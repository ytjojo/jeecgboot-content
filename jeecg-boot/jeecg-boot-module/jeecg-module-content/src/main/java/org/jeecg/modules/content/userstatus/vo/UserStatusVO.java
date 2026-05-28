package org.jeecg.modules.content.userstatus.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 用户状态查询响应。
 */
@Data
@Schema(description = "用户状态查询响应")
public class UserStatusVO {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "当前状态")
    private String status;

    @Schema(description = "状态显示名称")
    private String statusDisplayName;

    @Schema(description = "状态开始时间")
    private Date statusStartTime;

    @Schema(description = "状态结束时间")
    private Date statusEndTime;

    @Schema(description = "状态原因")
    private String statusReason;

    @Schema(description = "操作人ID")
    private String statusOperatorId;
}
