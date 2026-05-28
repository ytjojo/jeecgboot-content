package org.jeecg.modules.content.userstatus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用户状态审计日志实体。
 * 记录所有用户状态变更的完整审计日志。
 */
@Data
@Accessors(chain = true)
@TableName("content_user_status_audit_log")
@Schema(description = "用户状态审计日志")
public class UserStatusAuditLog {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "日志ID")
    private String logId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "原状态")
    private String fromStatus;

    @Schema(description = "新状态")
    private String toStatus;

    @Schema(description = "操作人ID（系统/管理员ID）")
    private String operatorId;

    @Schema(description = "操作人类型（SYSTEM/ADMIN）")
    private String operatorType;

    @Schema(description = "触发原因")
    private String triggerReason;

    @Schema(description = "规则ID（可选）")
    private String ruleId;

    @Schema(description = "状态开始时间")
    private Date startTime;

    @Schema(description = "状态结束时间")
    private Date endTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "操作人IP地址")
    private String ipAddress;

    @Schema(description = "创建时间")
    private Date createdAt;
}
