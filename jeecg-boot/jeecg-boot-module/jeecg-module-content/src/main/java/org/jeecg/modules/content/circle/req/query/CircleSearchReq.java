package org.jeecg.modules.content.circle.req.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "圈子搜索请求")
public class CircleSearchReq {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码(从1开始)")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页最少1条")
    @Max(value = 100, message = "每页最多100条")
    @Schema(description = "每页条数")
    private Integer pageSize = 20;
}
