package org.jeecg.modules.content.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 注销申请实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_cancellation_request")
public class ContentCancellationRequest extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "申请状态")
    private String status;

    @Schema(description = "申请原因")
    private String applyReason;

    @Schema(description = "申请时间")
    private Date applyTime;

    @Schema(description = "冷静期天数")
    private Integer cooldownDays;

    @Schema(description = "冷静期截止时间")
    private Date cooldownDeadline;

    @Schema(description = "注销原因")
    private String cancelReason;

    @Schema(description = "撤回时间")
    private Date revokeTime;

    @Schema(description = "完成时间")
    private Date completeTime;

    @Schema(description = "操作人用户ID")
    private String operatorUserId;

    @Schema(description = "是否已匿名化")
    private Boolean anonymized;
}
