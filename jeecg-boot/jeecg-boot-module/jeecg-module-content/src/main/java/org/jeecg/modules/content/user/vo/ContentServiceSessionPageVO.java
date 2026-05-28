package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 客服会话分页视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "客服会话分页视图")
public class ContentServiceSessionPageVO {

    @Schema(description = "会话列表")
    private List<ContentServiceSessionVO> records;

    @Schema(description = "总数")
    private long total;

    @Schema(description = "当前页")
    private long pageNo;

    @Schema(description = "每页大小")
    private long pageSize;
}
