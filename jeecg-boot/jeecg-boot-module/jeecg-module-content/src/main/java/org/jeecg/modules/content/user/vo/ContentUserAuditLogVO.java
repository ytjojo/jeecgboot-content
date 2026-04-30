package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * View object for content user audit log.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户审计日志视图")
public class ContentUserAuditLogVO {

    @Schema(description = "关联用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String userId;

    @Schema(description = "事件类型", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String eventType;

    @Schema(description = "事件内容", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String eventContent;

    @Schema(description = "操作人用户ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String operatorUserId;

    @Schema(description = "事件时间", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Date eventTime;
}
