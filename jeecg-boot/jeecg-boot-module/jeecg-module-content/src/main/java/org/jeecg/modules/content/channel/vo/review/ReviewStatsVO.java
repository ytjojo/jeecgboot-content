package org.jeecg.modules.content.channel.vo.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审核统计VO")
public class ReviewStatsVO {

    @Schema(description = "待审核数量")
    private Long pendingCount;

    @Schema(description = "超时未审核数量(超过24小时)")
    private Long timeoutCount;

    @Schema(description = "今日已通过数量")
    private Long todayApprovedCount;

    @Schema(description = "今日已拒绝数量")
    private Long todayRejectedCount;
}
