package org.jeecg.modules.content.circle.growth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "成就徽章信息")
public class AchievementVO {

    @Schema(description = "徽章类型")
    private String achievementType;

    @Schema(description = "徽章名称")
    private String name;

    @Schema(description = "徽章描述")
    private String description;

    @Schema(description = "徽章图标URL")
    private String iconUrl;

    @Schema(description = "是否已获得")
    private Boolean earned;

    @Schema(description = "获得时间")
    private Date earnedDate;

    @Schema(description = "达成条件描述")
    private String conditionDesc;

    @Schema(description = "当前进度数值")
    private Integer currentProgress;

    @Schema(description = "目标数值")
    private Integer targetProgress;

    @Schema(description = "状态: EARNED/CLOSE/UNEARNED")
    private String status;
}
