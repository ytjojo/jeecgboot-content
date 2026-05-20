package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 订阅源更新通知决策。
 */
@Data
@Accessors(chain = true)
@Schema(description = "订阅源更新通知决策")
public class ContentSubscriptionNotificationDecisionVO {

    @Schema(description = "是否发送实时通知")
    private Boolean realtimeDelivery;

    @Schema(description = "是否进入每日摘要")
    private Boolean dailySummary;

    @Schema(description = "是否因免打扰延迟")
    private Boolean delayedByDnd;

    @Schema(description = "通知渠道")
    private List<String> channels;

    @Schema(description = "更新业务ID")
    private String updateBizId;
}
