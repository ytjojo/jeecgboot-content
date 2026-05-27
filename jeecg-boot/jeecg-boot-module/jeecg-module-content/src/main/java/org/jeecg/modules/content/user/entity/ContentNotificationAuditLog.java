package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 通知发送审计日志实体。
 * 记录每次 canSendNotice 调用的决策结果，用于排查通知丢失问题。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_notification_audit_log")
@Schema(description = "通知发送审计日志")
public class ContentNotificationAuditLog extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "通知类型")
    private String noticeType;

    @Schema(description = "通知渠道")
    private String channel;

    @Schema(description = "决策结果：SEND/SKIP")
    private String decision;

    @Schema(description = "决策原因")
    private String reason;
}
