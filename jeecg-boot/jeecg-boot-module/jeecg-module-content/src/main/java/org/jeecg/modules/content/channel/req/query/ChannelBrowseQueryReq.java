package org.jeecg.modules.content.channel.req.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分类浏览查询请求")
public class ChannelBrowseQueryReq {

    @Schema(description = "分类ID")
    private String categoryId;

    @Schema(description = "排序方式: SUBSCRIBER_COUNT/ACTIVITY/CREATE_TIME")
    private String sortBy = "SUBSCRIBER_COUNT";

    @Schema(description = "频道类型筛选: PERSONAL/ORGANIZATION/SYSTEM")
    private String channelType;

    @Schema(description = "页码")
    private Integer pageNo = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 20;
}
