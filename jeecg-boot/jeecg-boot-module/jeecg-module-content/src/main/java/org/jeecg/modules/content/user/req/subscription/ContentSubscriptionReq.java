package org.jeecg.modules.content.user.req.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Request model for content subscription.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区订阅请求")
public class ContentSubscriptionReq {

    @NotBlank(message = "订阅源类型不能为空")
    @Size(max = 32, message = "订阅源类型长度不能超过32位")
    @Schema(description = "订阅源类型", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String sourceType;

    @NotBlank(message = "订阅源ID不能为空")
    @Size(max = 64, message = "订阅源ID长度不能超过64位")
    @Schema(description = "订阅源ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String sourceId;

    @NotBlank(message = "订阅源名称不能为空")
    @Size(max = 64, message = "订阅源名称长度不能超过64位")
    @Schema(description = "订阅源名称", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String sourceName;

    @Size(max = 255, message = "通知渠道配置长度不能超过255位")
    @Schema(description = "通知渠道配置", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String notificationChannels;

    @Size(max = 32, message = "通知频率长度不能超过32位")
    @Schema(description = "通知频率", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String notificationFrequency;
}
