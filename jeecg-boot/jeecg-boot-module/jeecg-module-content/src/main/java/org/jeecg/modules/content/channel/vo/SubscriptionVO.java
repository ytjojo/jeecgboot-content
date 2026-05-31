package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "订阅详情")
public class SubscriptionVO {

    @Schema(description = "订阅ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道名称")
    private String channelName;

    @Schema(description = "频道图标")
    private String channelIconUrl;

    @Schema(description = "订阅来源: 1=主动订阅, 2=默认关注")
    private Integer source;

    @Schema(description = "提醒开关: 0=关闭, 1=开启")
    private Integer remindEnabled;

    @Schema(description = "订阅时间")
    private LocalDateTime createTime;
}
