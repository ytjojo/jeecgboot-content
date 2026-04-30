package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * View object for content user status.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户状态视图")
public class ContentUserStatusVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String userId;

    @Schema(description = "当前状态", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String currentStatus;

    @Schema(description = "目标状态", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String targetStatus;

    @Schema(description = "状态变更原因", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String reason;

    @Schema(description = "生效开始时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Date effectiveStartTime;

    @Schema(description = "生效结束时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Date effectiveEndTime;
}
