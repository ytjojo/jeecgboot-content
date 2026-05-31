package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 圈子审核日志实体。
 * 记录圈子内所有审核操作的完整审计日志。
 */
@Data
@Accessors(chain = true)
@TableName("circle_audit_log")
@Schema(description = "圈子审核日志")
public class CircleAuditLog {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "日志ID")
    private String logId;

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "操作类型（来自CircleAuditActionEnum）")
    private String action;

    @Schema(description = "目标ID")
    private String targetId;

    @Schema(description = "目标类型（CONTENT/ANNOUNCEMENT/JOIN_REQUEST/REPORT/USER/CIRCLE）")
    private String targetType;

    @Schema(description = "操作结果")
    private String result;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "创建时间")
    private Date createdAt;
}
