package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Admin list item view for user reports.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区举报后台列表项视图")
public class ContentUserReportAdminListItemVO {

    @Schema(description = "举报ID")
    private String reportId;

    @Schema(description = "举报用户ID")
    private String userId;

    @Schema(description = "举报目标类型")
    private String targetType;

    @Schema(description = "举报目标ID")
    private String targetId;

    @Schema(description = "举报类型")
    private String reportType;

    @Schema(description = "举报状态")
    private String status;

    @Schema(description = "处理结果状态")
    private String resultStatus;

    @Schema(description = "处理人")
    private String resolvedBy;

    @Schema(description = "处理完成时间")
    private Date resolvedAt;

    @Schema(description = "创建时间")
    private Date createTime;
}
