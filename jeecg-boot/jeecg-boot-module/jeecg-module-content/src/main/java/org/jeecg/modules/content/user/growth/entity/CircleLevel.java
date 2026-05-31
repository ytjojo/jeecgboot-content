package org.jeecg.modules.content.user.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 圈子等级实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_level")
@Schema(description = "圈子等级")
public class CircleLevel extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "等级 1-5")
    private Integer level;

    @Schema(description = "成长分 0-1000")
    private Integer growthScore;

    @Schema(description = "成员规模得分")
    private Integer memberScore;

    @Schema(description = "内容贡献得分")
    private Integer contentScore;

    @Schema(description = "活跃互动得分")
    private Integer activityScore;
}
