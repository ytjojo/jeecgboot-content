package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentSubscriptionSource;

/**
 * 订阅源详情响应。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订阅源详情响应")
public class ContentSubscriptionSourceDetailVO extends ContentSubscriptionSourceVO {

    @Schema(description = "最近内容摘要")
    private String recentContentSummary;

    @Schema(description = "是否暂停订阅")
    private Boolean paused;

    public static ContentSubscriptionSourceDetailVO from(ContentSubscriptionSource source, ContentUserSubscriptionVO subscription) {
        ContentSubscriptionSourceVO base = ContentSubscriptionSourceVO.from(source, subscription);
        if (base == null) {
            return null;
        }
        ContentSubscriptionSourceDetailVO detail = new ContentSubscriptionSourceDetailVO()
            .setRecentContentSummary(source.getSourceDescription())
            .setPaused(subscription == null ? null : subscription.getPaused());
        detail.setSourceType(base.getSourceType());
        detail.setSourceId(base.getSourceId());
        detail.setSourceName(base.getSourceName());
        detail.setSourceDescription(base.getSourceDescription());
        detail.setCategory(base.getCategory());
        detail.setCoverUrl(base.getCoverUrl());
        detail.setSubscriberCount(base.getSubscriberCount());
        detail.setHeatScore(base.getHeatScore());
        detail.setLatestUpdateTime(base.getLatestUpdateTime());
        detail.setSubscribed(base.getSubscribed());
        detail.setSubscriptionId(base.getSubscriptionId());
        return detail;
    }
}
