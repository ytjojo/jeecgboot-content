package org.jeecg.modules.content.channel.req.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "查询分类请求")
public class ChannelCategoryQueryReq {

    @Schema(description = "父级分类ID")
    private String parentId;

    @Schema(description = "状态 0=停用 1=启用")
    private Integer status;
}
