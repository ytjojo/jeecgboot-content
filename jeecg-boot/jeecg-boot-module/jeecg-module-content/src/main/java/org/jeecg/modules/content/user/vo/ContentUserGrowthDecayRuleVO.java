package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 内容社区成长值衰减规则说明。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区成长值衰减规则说明")
public class ContentUserGrowthDecayRuleVO {

    @Schema(description = "是否启用衰减")
    private Boolean enabled;

    @Schema(description = "连续未活跃天数阈值")
    private Integer inactiveDays;

    @Schema(description = "单次衰减比例")
    private BigDecimal decayRate;

    @Schema(description = "降级保护天数")
    private Integer protectionDays;

    @Schema(description = "规则说明")
    private String ruleDescription;
}
