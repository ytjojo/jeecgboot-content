package org.jeecg.modules.content.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区统一奖励事件请求 DTO。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区统一奖励事件请求")
public class ContentUserRewardEventDTO {

    @NotBlank(message = "用户ID不能为空")
    @Size(max = 64, message = "用户ID长度不能超过64位")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    @NotBlank(message = "来源类型不能为空")
    @Size(max = 64, message = "来源类型长度不能超过64位")
    @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceType;

    @NotBlank(message = "奖励事件ID不能为空")
    @Size(max = 64, message = "奖励事件ID长度不能超过64位")
    @Schema(description = "奖励事件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventId;

    @Size(max = 64, message = "业务ID长度不能超过64位")
    @Schema(description = "业务ID")
    private String bizId;

    @Pattern(regexp = "^$|^\\d{8}$", message = "每日统计桶必须为yyyyMMdd")
    @Schema(description = "每日统计桶")
    private String dailyBucket;

    @Schema(description = "兼容旧入口的显式积分奖励")
    private Integer pointAmount;

    @Schema(description = "兼容旧入口的显式成长值奖励")
    private Integer growthAmount;

    @Schema(description = "是否跳过规则配置，仅用于兼容旧入口")
    private Boolean legacyDirectAward;
}
