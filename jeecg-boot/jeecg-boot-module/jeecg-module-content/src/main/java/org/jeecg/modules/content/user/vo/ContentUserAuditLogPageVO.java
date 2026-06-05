package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 审计日志分页查询结果。
 */
@Data
@Accessors(chain = true)
@Schema(description = "审计日志分页结果")
public class ContentUserAuditLogPageVO {

    @Schema(description = "分页记录")
    private List<ContentUserAuditLogItemVO> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Long pageNo;

    @Schema(description = "每页条数")
    private Long pageSize;
}
