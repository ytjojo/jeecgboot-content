package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * View object for a single status history record.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户状态历史明细")
public class ContentUserStatusHistoryItemVO {

    @Schema(description = "状态记录ID")
    private String recordId;

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

    @Schema(description = "创建时间")
    private Date createTime;
}
