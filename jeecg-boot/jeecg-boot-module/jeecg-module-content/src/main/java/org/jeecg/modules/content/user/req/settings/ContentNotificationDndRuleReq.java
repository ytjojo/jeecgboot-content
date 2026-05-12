package org.jeecg.modules.content.user.req.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区通知免打扰规则请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区通知免打扰规则请求")
public class ContentNotificationDndRuleReq {

    @Schema(description = "是否启用免打扰", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean enabled;

    @Pattern(regexp = "^$|^([01]\\d|2[0-3]):[0-5]\\d$", message = "免打扰开始时间格式不合法")
    @Schema(description = "开始时间，格式HH:mm", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String startTime;

    @Pattern(regexp = "^$|^([01]\\d|2[0-3]):[0-5]\\d$", message = "免打扰结束时间格式不合法")
    @Schema(description = "结束时间，格式HH:mm", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String endTime;
}
