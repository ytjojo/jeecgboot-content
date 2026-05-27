package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 内容社区通知免打扰规则视图。
 * 支持多时段配置，每个规则包含启用状态、时间段、日期类型、摘要模式。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区通知免打扰规则视图")
public class ContentNotificationDndRuleVO {

    @Schema(description = "是否启用免打扰")
    private Boolean enabled;

    @Schema(description = "开始时间，格式HH:mm（单时段兼容字段）")
    private String startTime;

    @Schema(description = "结束时间，格式HH:mm（单时段兼容字段）")
    private String endTime;

    @Schema(description = "多时段免打扰规则列表")
    private List<DndRuleItem> dndRules;

    @Schema(description = "临时关闭免打扰截止时间（Unix毫秒时间戳），null表示未启用临时关闭")
    private Long temporaryDisableUntil;

    /**
     * 单条免打扰规则。
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "单条免打扰规则")
    public static class DndRuleItem {

        @Schema(description = "是否启用该条规则")
        private Boolean enabled;

        @Schema(description = "开始时间，格式HH:mm")
        private String startTime;

        @Schema(description = "结束时间，格式HH:mm")
        private String endTime;

        @Schema(description = "日期类型：DAILY/WORKDAY/WEEKEND/CUSTOM")
        private String dayType;

        @Schema(description = "免打扰结束时是否发送通知摘要")
        private Boolean summaryMode;
    }
}
