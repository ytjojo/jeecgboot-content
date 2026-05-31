package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Schema(description = "用户分析响应")
public class ChannelUserAnalysisVO {

    @Schema(description = "新增订阅数")
    private Integer newSubscriberCount;

    @Schema(description = "流失订阅数")
    private Integer lostSubscriberCount;

    @Schema(description = "成员活跃度占比")
    private Map<String, Integer> activityDistribution;

    @Schema(description = "贡献排行")
    private List<Map<String, Object>> contributionRanking;
}
