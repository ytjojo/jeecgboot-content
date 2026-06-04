package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用户级衰减状态查询结果。
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户级衰减状态")
public class ContentUserGrowthDecayStatusVO {

    @Schema(description = "衰减状态：NORMAL/DECAYING/PROTECTING/DOWNGRADED")
    private String status;

    @Schema(description = "连续未活跃天数")
    private Integer inactiveDays;

    @Schema(description = "降级保护截止时间")
    private Date protectionUntil;

    @Schema(description = "当前等级")
    private Integer currentLevel;

    @Schema(description = "当前成长值")
    private Integer currentGrowthValue;

    @Schema(description = "最后活跃时间")
    private Date lastActiveTime;

    @Schema(description = "衰减次数")
    private Integer decayCount;
}
