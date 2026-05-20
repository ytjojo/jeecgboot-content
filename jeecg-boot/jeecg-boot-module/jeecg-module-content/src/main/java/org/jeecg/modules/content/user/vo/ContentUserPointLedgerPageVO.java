package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 内容社区积分明细分页结果。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区积分明细分页结果")
public class ContentUserPointLedgerPageVO {

    @Schema(description = "当前页")
    private Long current;

    @Schema(description = "每页条数")
    private Long size;

    @Schema(description = "总条数")
    private Long total;

    @Schema(description = "明细记录")
    private List<ContentUserPointLedgerVO> records;
}
