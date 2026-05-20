package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 内容社区用户等级推荐分发权重视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户等级推荐分发权重")
public class ContentUserDistributionWeightVO {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "当前等级")
    private Integer level;

    @Schema(description = "内容质量分")
    private BigDecimal qualityScore;

    @Schema(description = "等级加权系数")
    private BigDecimal distributionWeight;

    @Schema(description = "是否要求质量评分")
    private Boolean qualityScoreRequired;

    @Schema(description = "是否应用等级加权")
    private Boolean weighted;
}
