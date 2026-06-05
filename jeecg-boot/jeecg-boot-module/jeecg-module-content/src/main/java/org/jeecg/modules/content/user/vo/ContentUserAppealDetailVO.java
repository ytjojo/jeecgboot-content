package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用户端申诉详情视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户端申诉详情视图")
public class ContentUserAppealDetailVO {

    @Schema(description = "申诉ID")
    private String appealId;

    @Schema(description = "申诉类型")
    private String appealType;

    @Schema(description = "申诉目标类型")
    private String targetType;

    @Schema(description = "申诉目标ID")
    private String targetId;

    @Schema(description = "申诉原因")
    private String reason;

    @Schema(description = "申诉状态")
    private String status;

    @Schema(description = "处理进度说明")
    private String progressNote;

    @Schema(description = "处理结果状态")
    private String resultStatus;

    @Schema(description = "处理结果说明")
    private String resultNote;

    @Schema(description = "处理人")
    private String resolvedBy;

    @Schema(description = "处理完成时间")
    private Date resolvedAt;

    @Schema(description = "创建时间")
    private Date createTime;
}
