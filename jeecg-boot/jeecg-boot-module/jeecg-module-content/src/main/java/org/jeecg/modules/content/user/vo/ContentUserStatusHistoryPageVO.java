package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Paged response model for user status history queries.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户状态历史分页结果")
public class ContentUserStatusHistoryPageVO {

    @Schema(description = "分页记录")
    private List<ContentUserStatusHistoryItemVO> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Long pageNo;

    @Schema(description = "每页条数")
    private Long pageSize;
}
