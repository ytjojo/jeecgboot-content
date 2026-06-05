package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 用户端举报分页视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户端举报分页视图")
public class ContentUserReportPageVO {

    @Schema(description = "举报列表")
    private List<ContentUserReportListItemVO> records;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "当前页码")
    private Long pageNo;

    @Schema(description = "每页大小")
    private Long pageSize;
}
