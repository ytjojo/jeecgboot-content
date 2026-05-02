package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区帮助中心条目视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区帮助中心条目视图")
public class ContentHelpCenterEntryVO {

    @Schema(description = "条目标识", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String code;

    @Schema(description = "条目标题", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String title;

    @Schema(description = "条目说明", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String description;

    @Schema(description = "推荐客服路由类型")
    private String recommendedRouteType;

    @Schema(description = "推荐客服路由标题")
    private String recommendedRouteTitle;

    @Schema(description = "是否支持人工客服")
    private Boolean manualSupported;
}
