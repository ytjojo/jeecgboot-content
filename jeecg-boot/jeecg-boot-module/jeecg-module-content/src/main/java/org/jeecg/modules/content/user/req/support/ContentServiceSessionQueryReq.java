package org.jeecg.modules.content.user.req.support;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 客服会话查询请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "客服会话查询请求")
public class ContentServiceSessionQueryReq {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "页码")
    private Long pageNo;

    @Schema(description = "每页大小")
    private Long pageSize;
}
