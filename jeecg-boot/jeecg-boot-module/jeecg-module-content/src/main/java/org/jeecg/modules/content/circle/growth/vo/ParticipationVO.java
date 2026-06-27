package org.jeecg.modules.content.circle.growth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Schema(description = "连续参与进度")
public class ParticipationVO {

    @Schema(description = "连续参与天数")
    private Integer days;

    @Schema(description = "近 7 天每日状态（下标 0 = 今天，6 = 7 天前）")
    private List<DayStatus> dailyStatus;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayStatus {
        @Schema(description = "日期 yyyy-MM-dd")
        private String date;

        @Schema(description = "是否参与")
        private Boolean participated;
    }
}
