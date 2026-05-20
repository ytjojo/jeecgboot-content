package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区用户等级配置视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户等级配置视图")
public class ContentUserLevelConfigVO {

    @Schema(description = "等级", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Integer level;

    @Schema(description = "等级名称", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String levelName;

    @Schema(description = "成长值门槛", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Integer growthThreshold;

    @Schema(description = "等级标识样式KEY", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String badgeStyleKey;
}
