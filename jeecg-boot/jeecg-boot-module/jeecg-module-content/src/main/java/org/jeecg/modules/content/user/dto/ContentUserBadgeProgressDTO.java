package org.jeecg.modules.content.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区用户勋章进度 DTO。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户勋章进度")
public class ContentUserBadgeProgressDTO {

    @Schema(description = "勋章编码")
    private String badgeCode;

    @Schema(description = "进度指标")
    private String metric;

    @Schema(description = "当前进度")
    private Integer currentProgress;

    @Schema(description = "目标进度")
    private Integer targetProgress;

    @Schema(description = "剩余要求")
    private Integer remainingRequirement;
}
