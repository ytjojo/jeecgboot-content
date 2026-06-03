package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容社区用户屏蔽规则分页结果。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户屏蔽规则分页结果")
public class ContentFilterRulePageVO {

    @Schema(description = "列表数据")
    private List<ContentFilterRuleItemVO> records = new ArrayList<>();

    @Schema(description = "总数")
    private Long total = 0L;

    @Schema(description = "页码")
    private Long pageNo = 1L;

    @Schema(description = "每页数量")
    private Long pageSize = 10L;
}
