package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.modules.content.channel.enums.ChannelStatus;

@Data
@Schema(description = "频道可见性判断参数")
public class ChannelVisibilityDTO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道状态")
    private ChannelStatus status;

    @Schema(description = "隐私设置: 1=公开, 2=私有")
    private Integer privacy;
}
