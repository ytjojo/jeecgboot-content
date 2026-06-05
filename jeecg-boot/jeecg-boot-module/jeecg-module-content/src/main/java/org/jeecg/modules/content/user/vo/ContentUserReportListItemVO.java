package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用户端举报列表项视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户端举报列表项视图")
public class ContentUserReportListItemVO {

    @Schema(description = "举报ID")
    private String reportId;

    @Schema(description = "举报编号")
    private String reportNo;

    @Schema(description = "举报目标摘要")
    private String targetSummary;

    @Schema(description = "举报类型标签")
    private String reportTypeLabel;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "举报状态")
    private String status;

    @Schema(description = "处理结果状态")
    private String resultStatus;

    @Schema(description = "创建时间")
    private Date createTime;
}
