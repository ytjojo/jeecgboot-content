package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用户端举报详情视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户端举报详情视图")
public class ContentUserReportDetailVO {

    @Schema(description = "举报ID")
    private String reportId;

    @Schema(description = "举报编号")
    private String reportNo;

    @Schema(description = "举报目标类型")
    private String targetType;

    @Schema(description = "举报目标ID")
    private String targetId;

    @Schema(description = "举报目标摘要")
    private String targetSummary;

    @Schema(description = "举报类型")
    private String reportType;

    @Schema(description = "举报类型标签")
    private String reportTypeLabel;

    @Schema(description = "举报原因")
    private String reason;

    @Schema(description = "举报证据JSON")
    private String evidenceJson;

    @Schema(description = "举报状态")
    private String status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "处理结果状态")
    private String resultStatus;

    @Schema(description = "处理结果说明")
    private String resultNote;

    @Schema(description = "处理进度说明")
    private String progressNote;

    @Schema(description = "处理人")
    private String resolvedBy;

    @Schema(description = "处理完成时间")
    private Date resolvedAt;

    @Schema(description = "创建时间")
    private Date createTime;
}
