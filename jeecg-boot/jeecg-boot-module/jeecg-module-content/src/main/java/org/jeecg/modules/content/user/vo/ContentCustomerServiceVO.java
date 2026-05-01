package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * View object for customer service entry.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区客服入口视图")
public class ContentCustomerServiceVO {

    @Schema(description = "客服路由类型", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String routeType;

    @Schema(description = "客服通道标题", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String title;

    @Schema(description = "客服通道说明", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String description;

    @Schema(description = "是否开放人工客服", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Boolean manualSupported;
}
