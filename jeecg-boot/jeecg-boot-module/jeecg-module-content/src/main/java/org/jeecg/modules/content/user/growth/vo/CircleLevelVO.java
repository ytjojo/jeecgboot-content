package org.jeecg.modules.content.user.growth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "圈子等级信息")
public class CircleLevelVO {

    @Schema(description = "当前等级")
    private Integer level;

    @Schema(description = "等级名称")
    private String levelName;

    @Schema(description = "成长分")
    private Integer growthScore;

    @Schema(description = "下一等级门槛")
    private Integer nextLevelThreshold;

    @Schema(description = "进度百分比")
    private Integer progressPercent;
}
