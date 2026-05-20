package org.jeecg.modules.content.user.req.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 订阅级通知偏好请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "订阅级通知偏好请求")
public class ContentSubscriptionNotificationPreferenceReq {

    @Schema(description = "订阅ID")
    private String subscriptionId;

    @Schema(description = "通知渠道，支持 IN_APP、PUSH、EMAIL")
    private List<String> notificationChannels;

    @Schema(description = "通知频率，支持 REALTIME、DAILY")
    private String notificationFrequency;

    @Schema(description = "免打扰开始时间，格式 HH:mm")
    private String dndStartTime;

    @Schema(description = "免打扰结束时间，格式 HH:mm")
    private String dndEndTime;
}
