package org.jeecg.modules.content.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区统一奖励处理结果 DTO。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区统一奖励处理结果")
public class ContentUserRewardResultDTO {

    @Schema(description = "奖励事件ID")
    private String eventId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "命中奖励规则编码")
    private String ruleCode;

    @Schema(description = "实际奖励积分")
    private Integer pointDelta;

    @Schema(description = "实际奖励成长值")
    private Integer growthDelta;

    @Schema(description = "处理状态")
    private String processStatus;

    @Schema(description = "跳过原因")
    private String skipReason;

    @Schema(description = "是否重复事件")
    private Boolean duplicate;

    /**
     * 根据奖励事件实体字段创建返回结果。
     */
    public static ContentUserRewardResultDTO of(String eventId, String userId, String sourceType, String ruleCode,
                                                Integer pointDelta, Integer growthDelta, String processStatus,
                                                String skipReason, boolean duplicate) {
        return new ContentUserRewardResultDTO()
            .setEventId(eventId)
            .setUserId(userId)
            .setSourceType(sourceType)
            .setRuleCode(ruleCode)
            .setPointDelta(pointDelta == null ? 0 : pointDelta)
            .setGrowthDelta(growthDelta == null ? 0 : growthDelta)
            .setProcessStatus(processStatus)
            .setSkipReason(skipReason)
            .setDuplicate(duplicate);
    }
}
