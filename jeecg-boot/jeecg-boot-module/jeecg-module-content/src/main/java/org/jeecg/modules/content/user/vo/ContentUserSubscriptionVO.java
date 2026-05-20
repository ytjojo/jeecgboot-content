package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserSubscription;

import java.util.Date;

/**
 * 用户订阅响应。
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户订阅响应")
public class ContentUserSubscriptionVO {

    @Schema(description = "订阅ID")
    private String subscriptionId;

    @Schema(description = "订阅源类型")
    private String sourceType;

    @Schema(description = "订阅源ID")
    private String sourceId;

    @Schema(description = "订阅源名称")
    private String sourceName;

    @Schema(description = "订阅时间")
    private Date subscribedAt;

    @Schema(description = "最近更新时间")
    private Date lastUpdateTime;

    @Schema(description = "是否暂停")
    private Boolean paused;

    @Schema(description = "订阅状态")
    private String subscriptionStatus;

    @Schema(description = "通知摘要")
    private String notificationSummary;

    public static ContentUserSubscriptionVO from(ContentUserSubscription subscription) {
        if (subscription == null) {
            return null;
        }
        return new ContentUserSubscriptionVO()
            .setSubscriptionId(subscription.getId())
            .setSourceType(subscription.getSourceType())
            .setSourceId(subscription.getSourceId())
            .setSourceName(subscription.getSourceName())
            .setSubscribedAt(subscription.getSubscribedAt())
            .setLastUpdateTime(subscription.getLastUpdateTime())
            .setPaused(subscription.getPaused())
            .setSubscriptionStatus(subscription.getSubscriptionStatus())
            .setNotificationSummary(buildNotificationSummary(subscription));
    }

    private static String buildNotificationSummary(ContentUserSubscription subscription) {
        String channels = subscription.getNotificationChannels() == null ? "默认渠道" : subscription.getNotificationChannels();
        String frequency = subscription.getNotificationFrequency() == null ? "默认频率" : subscription.getNotificationFrequency();
        return channels + "/" + frequency;
    }
}
