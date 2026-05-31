package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "频道申诉记录VO")
public class ChannelAppealVO {

    @Schema(description = "申诉ID")
    private String appealId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "关联生命周期日志ID")
    private String lifecycleLogId;

    @Schema(description = "申诉人ID")
    private String applicantId;

    @Schema(description = "申诉理由")
    private String appealReason;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "处理人ID")
    private String handlerId;

    @Schema(description = "处理结果")
    private String handleResult;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "首次响应时间")
    private LocalDateTime firstResponseTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
