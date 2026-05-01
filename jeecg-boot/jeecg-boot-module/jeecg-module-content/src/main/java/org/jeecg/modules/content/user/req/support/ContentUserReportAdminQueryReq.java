package org.jeecg.modules.content.user.req.support;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Request model for admin-side report page query.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区举报后台分页查询请求")
public class ContentUserReportAdminQueryReq {

    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码")
    private Long pageNo;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    @Schema(description = "每页条数")
    private Long pageSize;

    @Schema(description = "举报状态")
    private String status;

    @Schema(description = "处理结果状态")
    private String resultStatus;

    @Schema(description = "举报用户ID")
    private String userId;

    @Schema(description = "举报目标类型")
    private String targetType;

    @Schema(description = "举报目标ID")
    private String targetId;

    @Schema(description = "举报类型")
    private String reportType;

    @Schema(description = "处理人")
    private String resolvedBy;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建开始时间")
    private Date createTimeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建结束时间")
    private Date createTimeEnd;
}
