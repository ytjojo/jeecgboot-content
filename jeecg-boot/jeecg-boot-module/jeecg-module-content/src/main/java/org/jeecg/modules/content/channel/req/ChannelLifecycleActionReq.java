package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "频道生命周期操作请求")
public class ChannelLifecycleActionReq {

    @Schema(description = "频道ID", required = true)
    private String channelId;

    @Schema(description = "操作原因")
    private String reason;
}
