package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Paged response model for admin-side report queries.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区举报后台分页结果")
public class ContentUserReportAdminPageVO {

    @Schema(description = "分页记录")
    private List<ContentUserReportAdminListItemVO> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Long pageNo;

    @Schema(description = "每页条数")
    private Long pageSize;
}
