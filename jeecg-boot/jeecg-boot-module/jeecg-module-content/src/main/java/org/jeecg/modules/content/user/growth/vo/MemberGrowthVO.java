package org.jeecg.modules.content.user.growth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

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

    @Schema(description = "下一等级门槛")
    private Integer nextLevelThreshold;

    @Schema(description = "等级进度百分比")
    private Integer progressPercent;

    @Schema(description = "今日已获经验值")
    private Integer todayExp;

    @Schema(description = "每日经验值上限")
    private Integer dailyExpLimit;

    @Schema(description = "最近获得的徽章（最多3枚）")
    private List<AchievementVO> recentBadges;
}
