package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentSubscriptionNotificationPreference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 订阅级通知偏好响应。
 */
@Data
@Accessors(chain = true)
@Schema(description = "订阅级通知偏好响应")
public class ContentSubscriptionNotificationPreferenceVO {

    @Schema(description = "订阅ID")
    private String subscriptionId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "通知渠道")
    private List<String> notificationChannels;

    @Schema(description = "通知频率")
    private String notificationFrequency;

    @Schema(description = "免打扰开始时间")
    private String dndStartTime;

    @Schema(description = "免打扰结束时间")
    private String dndEndTime;

    @Schema(description = "是否继承全局默认值")
    private Boolean inherited;

    public static ContentSubscriptionNotificationPreferenceVO from(ContentSubscriptionNotificationPreference preference, boolean inherited) {
        return new ContentSubscriptionNotificationPreferenceVO()
            .setSubscriptionId(preference.getSubscriptionId())
            .setUserId(preference.getUserId())
            .setNotificationChannels(split(preference.getNotificationChannels()))
            .setNotificationFrequency(preference.getNotificationFrequency())
            .setDndStartTime(preference.getDndStartTime())
            .setDndEndTime(preference.getDndEndTime())
            .setInherited(inherited);
    }

    private static List<String> split(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(text.split(",")).map(String::trim).filter(it -> !it.isEmpty()).toList();
    }
}
