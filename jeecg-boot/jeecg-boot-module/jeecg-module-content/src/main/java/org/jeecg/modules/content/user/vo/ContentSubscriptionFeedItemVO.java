package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserSubscription;

import java.util.Date;

/**
 * 订阅流更新项。
 */
@Data
@Accessors(chain = true)
@Schema(description = "订阅流更新项")
public class ContentSubscriptionFeedItemVO {

    @Schema(description = "订阅ID")
    private String subscriptionId;

    @Schema(description = "订阅源类型")
    private String sourceType;

    @Schema(description = "订阅源ID")
    private String sourceId;

    @Schema(description = "订阅源名称")
    private String sourceName;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "通知摘要")
    private String notificationSummary;

    public static ContentSubscriptionFeedItemVO from(ContentUserSubscription subscription) {
        if (subscription == null) {
            return null;
        }
        return new ContentSubscriptionFeedItemVO()
            .setSubscriptionId(subscription.getId())
            .setSourceType(subscription.getSourceType())
            .setSourceId(subscription.getSourceId())
            .setSourceName(subscription.getSourceName())
            .setUpdateTime(subscription.getLastUpdateTime())
            .setNotificationSummary(ContentUserSubscriptionVO.from(subscription).getNotificationSummary());
    }
}
