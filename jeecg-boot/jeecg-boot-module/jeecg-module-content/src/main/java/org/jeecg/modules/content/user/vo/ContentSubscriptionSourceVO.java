package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentSubscriptionSource;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订阅源目录响应。
 */
@Data
@Accessors(chain = true)
@Schema(description = "订阅源目录响应")
public class ContentSubscriptionSourceVO {

    @Schema(description = "订阅源类型")
    private String sourceType;

    @Schema(description = "订阅源ID")
    private String sourceId;

    @Schema(description = "订阅源名称")
    private String sourceName;

    @Schema(description = "订阅源介绍")
    private String sourceDescription;

    @Schema(description = "订阅源分类")
    private String category;

    @Schema(description = "封面地址")
    private String coverUrl;

    @Schema(description = "订阅人数")
    private Integer subscriberCount;

    @Schema(description = "热度分")
    private BigDecimal heatScore;

    @Schema(description = "最近更新时间")
    private Date latestUpdateTime;

    @Schema(description = "是否已订阅")
    private Boolean subscribed;

    @Schema(description = "当前订阅ID")
    private String subscriptionId;

    public static ContentSubscriptionSourceVO from(ContentSubscriptionSource source, ContentUserSubscriptionVO subscription) {
        if (source == null) {
            return null;
        }
        return new ContentSubscriptionSourceVO()
            .setSourceType(source.getSourceType())
            .setSourceId(source.getSourceId())
            .setSourceName(source.getSourceName())
            .setSourceDescription(source.getSourceDescription())
            .setCategory(source.getCategory())
            .setCoverUrl(source.getCoverUrl())
            .setSubscriberCount(source.getSubscriberCount())
            .setHeatScore(source.getHeatScore())
            .setLatestUpdateTime(source.getLatestUpdateTime())
            .setSubscribed(subscription != null && !"CANCELLED".equals(subscription.getSubscriptionStatus()))
            .setSubscriptionId(subscription == null ? null : subscription.getSubscriptionId());
    }
}
