package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "编辑精选VO")
public class ChannelEditorialPickVO {

    @Schema(description = "精选ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道名称")
    private String channelName;

    @Schema(description = "频道图标")
    private String channelIcon;

    @Schema(description = "推荐语")
    private String recommendationText;

    @Schema(description = "生效开始时间")
    private Date startTime;

    @Schema(description = "生效结束时间")
    private Date endTime;
}
