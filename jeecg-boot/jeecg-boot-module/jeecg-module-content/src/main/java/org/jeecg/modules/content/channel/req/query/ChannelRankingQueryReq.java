package org.jeecg.modules.content.channel.req.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "排行榜查询请求")
public class ChannelRankingQueryReq {

    @Schema(description = "维度: DAILY/WEEKLY/MONTHLY")
    private String dimension = "DAILY";
}
