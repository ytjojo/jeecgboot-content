package org.jeecg.modules.content.circle.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 内容举报视图对象。
 */
@Data
@Schema(description = "内容举报视图")
public class CircleReportVO {

    @Schema(description = "举报ID")
    private String id;

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "被举报的内容ID")
    private String contentId;

    @Schema(description = "举报人用户ID")
    private String reporterId;

    @Schema(description = "举报原因")
    private String reason;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "处理动作")
    private String handleAction;

    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
