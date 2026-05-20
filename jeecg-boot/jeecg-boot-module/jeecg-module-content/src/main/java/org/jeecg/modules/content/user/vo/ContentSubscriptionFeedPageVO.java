package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * 订阅流分页响应。
 */
@Data
@Accessors(chain = true)
@Schema(description = "订阅流分页响应")
public class ContentSubscriptionFeedPageVO {

    @Schema(description = "订阅流列表")
    private List<ContentSubscriptionFeedItemVO> records = Collections.emptyList();

    @Schema(description = "总数")
    private Long total = 0L;

    @Schema(description = "页码")
    private Long pageNo;

    @Schema(description = "分页大小")
    private Long pageSize;

    @Schema(description = "是否还有更多")
    private Boolean hasMore = Boolean.FALSE;
}
