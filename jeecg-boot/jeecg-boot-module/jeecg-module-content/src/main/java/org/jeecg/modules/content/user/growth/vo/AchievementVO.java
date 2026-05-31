package org.jeecg.modules.content.user.growth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "成就徽章信息")
public class AchievementVO {

    @Schema(description = "徽章类型")
    private String achievementType;

    @Schema(description = "徽章名称")
    private String name;

    @Schema(description = "徽章描述")
    private String description;

    @Schema(description = "是否已获得")
    private Boolean earned;

    @Schema(description = "达成条件描述")
    private String conditionDesc;
}
