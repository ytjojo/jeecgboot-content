package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "频道生命周期变更日志VO")
public class ChannelLifecycleLogVO {

    @Schema(description = "日志ID")
    private String logId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "操作类型")
    private String actionType;

    @Schema(description = "变更前状态")
    private String fromStatus;

    @Schema(description = "变更后状态")
    private String toStatus;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "影响范围")
    private String impactScope;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
