package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * View object for user appeal progress.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户申诉进度视图")
public class ContentUserAppealProgressVO {

    @Schema(description = "申诉ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String appealId;

    @Schema(description = "申诉状态", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String status;

    @Schema(description = "处理进度说明", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String progressNote;

    @Schema(description = "处理结果状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String resultStatus;

    @Schema(description = "处理结果说明", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String resultNote;

    @Schema(description = "处理人", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String resolvedBy;

    @Schema(description = "处理完成时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Date resolvedAt;
}
