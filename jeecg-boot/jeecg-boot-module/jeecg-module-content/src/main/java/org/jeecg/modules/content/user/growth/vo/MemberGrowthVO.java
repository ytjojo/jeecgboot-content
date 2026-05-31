package org.jeecg.modules.content.user.growth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "成员成长信息")
public class MemberGrowthVO {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "经验值")
    private Integer expPoints;

    @Schema(description = "贡献值")
    private Integer contributionPoints;

    @Schema(description = "成员等级")
    private Integer level;

    @Schema(description = "发帖数")
    private Integer postCount;

    @Schema(description = "连续参与天数")
    private Integer participationDays;

    @Schema(description = "圈内排名")
    private Integer rank;
}
