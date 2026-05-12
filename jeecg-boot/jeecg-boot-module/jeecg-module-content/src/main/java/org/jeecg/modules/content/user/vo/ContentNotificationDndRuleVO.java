package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区通知免打扰规则视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区通知免打扰规则视图")
public class ContentNotificationDndRuleVO {

    @Schema(description = "是否启用免打扰")
    private Boolean enabled;

    @Schema(description = "开始时间，格式HH:mm")
    private String startTime;

    @Schema(description = "结束时间，格式HH:mm")
    private String endTime;
}
