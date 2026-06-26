package org.jeecg.modules.content.circle.growth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

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

    @Schema(description = "全部权益列表（含已解锁/未解锁状态）")
    private List<CircleBenefitVO> benefits;

    @Schema(description = "成员规模得分")
    private Integer memberScore;

    @Schema(description = "内容贡献得分")
    private Integer contentScore;

    @Schema(description = "活跃互动得分")
    private Integer activityScore;

    @Schema(description = "下一等级各项条件")
    private List<LevelConditionVO> nextLevelConditions;
}
