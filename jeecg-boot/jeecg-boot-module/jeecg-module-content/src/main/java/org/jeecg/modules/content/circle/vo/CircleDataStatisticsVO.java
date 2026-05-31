package org.jeecg.modules.content.circle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "圈子数据统计VO")
public class CircleDataStatisticsVO {
    @Schema(description = "成员总数")
    private Integer memberCount;

    @Schema(description = "新增成员数")
    private Integer newMemberCount;

    @Schema(description = "帖子总数")
    private Integer postCount;

    @Schema(description = "新增帖子数")
    private Integer newPostCount;

    @Schema(description = "活跃用户数")
    private Integer activeCount;

    @Schema(description = "每日趋势数据")
    private List<DailyTrend> dailyTrends;

    @Data
    @Schema(description = "每日趋势")
    public static class DailyTrend {
        @Schema(description = "日期")
        private LocalDate date;

        @Schema(description = "新增成员数")
        private Integer newMemberCount;

        @Schema(description = "新增帖子数")
        private Integer newPostCount;

        @Schema(description = "活跃用户数")
        private Integer activeCount;
    }
}
