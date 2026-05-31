package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "频道标签VO")
public class ChannelTagVO {

    @Schema(description = "标签ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "标签名称")
    private String name;
}
