package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * Entity for content user subscription.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_subscription")
public class ContentUserSubscription extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "订阅源类型")
    private String sourceType;

    @Schema(description = "订阅源ID")
    private String sourceId;

    @Schema(description = "订阅源名称")
    private String sourceName;

    @Schema(description = "通知渠道配置")
    private String notificationChannels;

    @Schema(description = "通知频率")
    private String notificationFrequency;

    @Schema(description = "是否暂停订阅")
    private Boolean paused;

    @Schema(description = "推荐理由")
    private String recommendationReason;
}
