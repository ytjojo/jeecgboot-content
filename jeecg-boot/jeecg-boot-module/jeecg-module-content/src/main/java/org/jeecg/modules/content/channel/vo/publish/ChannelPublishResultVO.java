package org.jeecg.modules.content.channel.vo.publish;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "频道发布结果")
public class ChannelPublishResultVO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "发布状态：PUBLISHED/PENDING/FAILED")
    private String status;

    @Schema(description = "失败原因")
    private String failReason;
}
