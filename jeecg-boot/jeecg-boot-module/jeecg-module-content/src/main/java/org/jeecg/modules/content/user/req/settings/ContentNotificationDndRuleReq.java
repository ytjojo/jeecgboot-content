package org.jeecg.modules.content.user.req.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 内容社区通知免打扰规则请求。
 * 支持多时段配置，旧单时段字段仍兼容保留。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区通知免打扰规则请求")
public class ContentNotificationDndRuleReq {

    @Schema(description = "是否启用免打扰", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean enabled;

    @Pattern(regexp = "^$|^([01]\\d|2[0-3]):[0-5]\\d$", message = "免打扰开始时间格式不合法")
    @Schema(description = "开始时间，格式HH:mm（单时段兼容字段）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String startTime;

    @Pattern(regexp = "^$|^([01]\\d|2[0-3]):[0-5]\\d$", message = "免打扰结束时间格式不合法")
    @Schema(description = "结束时间，格式HH:mm（单时段兼容字段）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String endTime;

    @Valid
    @Schema(description = "多时段免打扰规则列表", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private List<DndRuleItemReq> dndRules;

    @Schema(description = "临时关闭免打扰（关闭1小时）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean temporaryDisable;

    /**
     * 单条免打扰规则请求。
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "单条免打扰规则请求")
    public static class DndRuleItemReq {

        @Schema(description = "是否启用该条规则", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
        private Boolean enabled;

        @Pattern(regexp = "^$|^([01]\\d|2[0-3]):[0-5]\\d$", message = "免打扰开始时间格式不合法")
        @Schema(description = "开始时间，格式HH:mm", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
        private String startTime;

        @Pattern(regexp = "^$|^([01]\\d|2[0-3]):[0-5]\\d$", message = "免打扰结束时间格式不合法")
        @Schema(description = "结束时间，格式HH:mm", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
        private String endTime;

        @Pattern(regexp = "^(DAILY|WORKDAY|WEEKEND|CUSTOM)$", message = "日期类型取值不合法")
        @Schema(description = "日期类型：DAILY/WORKDAY/WEEKEND/CUSTOM", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
        private String dayType;

        @Schema(description = "免打扰结束时是否发送通知摘要", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
        private Boolean summaryMode;
    }
}
