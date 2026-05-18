package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区订阅通知偏好。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_subscription_notification_preference")
@Schema(description = "内容社区订阅通知偏好")
public class ContentSubscriptionNotificationPreference extends JeecgEntity {

    @Schema(description = "订阅ID")
    private String subscriptionId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "通知渠道")
    private String notificationChannels;

    @Schema(description = "通知频率")
    private String notificationFrequency;

    @Schema(description = "免打扰开始时间")
    private String dndStartTime;

    @Schema(description = "免打扰结束时间")
    private String dndEndTime;

    @Schema(description = "偏好状态")
    private String preferenceStatus;
}
