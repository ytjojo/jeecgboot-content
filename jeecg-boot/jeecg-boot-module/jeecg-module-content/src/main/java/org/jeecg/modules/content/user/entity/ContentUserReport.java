package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.modules.content.user.req.support.ContentReportCreateReq;

import java.util.Date;

/**
 * Entity for content user report.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_report")
public class ContentUserReport extends JeecgEntity {

    @Schema(description = "举报用户ID")
    private String userId;

    @Schema(description = "举报目标类型")
    private String targetType;

    @Schema(description = "举报目标ID")
    private String targetId;

    @Schema(description = "举报类型")
    private String reportType;

    @Schema(description = "举报原因")
    private String reason;

    @Schema(description = "举报证据JSON")
    private String evidenceJson;

    @Schema(description = "举报状态")
    private String status;

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

    /**
     * Builds the current object from report create request.
     */
    public static ContentUserReport from(ContentReportCreateReq req) {
        return new ContentUserReport()
            .setUserId(req.getUserId())
            .setTargetType(req.getTargetType())
            .setTargetId(req.getTargetId())
            .setReportType(req.getReportType())
            .setReason(req.getReason())
            .setEvidenceJson(req.getEvidenceJson())
            .setStatus("PENDING");
    }
}
