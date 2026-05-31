package org.jeecg.modules.content.channel.req.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "推荐查询请求")
public class ChannelRecommendationQueryReq {

    @Schema(description = "页码")
    private Integer pageNo = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 20;
}
