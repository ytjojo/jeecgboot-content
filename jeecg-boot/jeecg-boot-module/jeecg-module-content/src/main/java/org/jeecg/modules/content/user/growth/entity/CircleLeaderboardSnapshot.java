package org.jeecg.modules.content.user.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.time.LocalDateTime;

/**
 * 排行榜快照实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_leaderboard_snapshot")
@Schema(description = "排行榜快照")
public class CircleLeaderboardSnapshot extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "维度: EXP/CONTRIBUTION/POST")
    private String dimension;

    @Schema(description = "周期: WEEK/MONTH/ALL")
    private String period;

    @Schema(description = "得分")
    private Integer score;

    @Schema(description = "排名")
    private Integer rankNum;

    @Schema(description = "快照时间")
    private LocalDateTime snapshotTime;
}
