package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * View object for content user growth.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户成长概览视图")
public class ContentUserGrowthVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String userId;

    @Schema(description = "积分余额", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Integer pointBalance;

    @Schema(description = "成长值", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Integer growthValue;

    @Schema(description = "等级", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Integer level;
}
