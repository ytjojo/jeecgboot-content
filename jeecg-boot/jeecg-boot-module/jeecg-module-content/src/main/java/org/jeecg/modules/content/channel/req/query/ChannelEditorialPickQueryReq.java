package org.jeecg.modules.content.channel.req.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "编辑精选分页查询请求")
public class ChannelEditorialPickQueryReq {

    @Schema(description = "状态筛选: 0=下线 1=上线, 不传则查全部")
    private Integer status;

    @Schema(description = "页码")
    private Integer pageNo = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 10;
}
