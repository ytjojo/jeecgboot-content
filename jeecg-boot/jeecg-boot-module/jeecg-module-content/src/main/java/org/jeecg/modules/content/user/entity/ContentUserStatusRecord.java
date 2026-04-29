package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;

import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_status_record")
@Schema(description = "内容社区用户状态记录")
public class ContentUserStatusRecord extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "当前状态")
    private String currentStatus;

    @Schema(description = "目标状态")
    private String targetStatus;

    @Schema(description = "触发来源")
    private String triggerSource;

    @Schema(description = "操作人")
    private String operatorUserId;

    @Schema(description = "变更原因")
    private String reason;

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "开始时间")
    private Date effectiveStartTime;

    @Schema(description = "结束时间")
    private Date effectiveEndTime;

    @Schema(description = "是否可恢复")
    private Boolean recoverable;

    public static ContentUserStatusRecord from(ContentUserStatusChangeReq req) {
        return new ContentUserStatusRecord()
            .setUserId(req.getUserId())
            .setCurrentStatus(req.getCurrentStatus())
            .setTargetStatus(req.getTargetStatus())
            .setTriggerSource("MANUAL")
            .setOperatorUserId(req.getOperatorUserId())
            .setReason(req.getReason())
            .setRuleCode(req.getRuleCode())
            .setEffectiveStartTime(req.getEffectiveStartTime() == null ? new Date() : req.getEffectiveStartTime())
            .setEffectiveEndTime(req.getEffectiveEndTime())
            .setRecoverable(Boolean.TRUE);
    }
}
