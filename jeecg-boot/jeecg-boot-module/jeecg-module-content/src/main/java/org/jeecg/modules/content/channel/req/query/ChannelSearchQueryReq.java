package org.jeecg.modules.content.channel.req.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "频道搜索请求")
public class ChannelSearchQueryReq {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "频道类型筛选: PERSONAL/ORGANIZATION/SYSTEM")
    private String channelType;

    @Schema(description = "分类ID筛选")
    private String categoryId;

    @Schema(description = "排序方式: RELEVANCE/ACTIVITY/SUBSCRIBER_COUNT")
    private String sortBy = "RELEVANCE";

    @Schema(description = "页码")
    private Integer pageNo = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 20;
}
