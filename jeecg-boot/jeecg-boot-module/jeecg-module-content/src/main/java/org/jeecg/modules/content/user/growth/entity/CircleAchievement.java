package org.jeecg.modules.content.user.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 成就徽章配置实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_achievement")
@Schema(description = "成就徽章配置")
public class CircleAchievement extends JeecgEntity {

    @Schema(description = "徽章类型")
    private String achievementType;

    @Schema(description = "徽章名称")
    private String name;

    @Schema(description = "徽章描述")
    private String description;

    @Schema(description = "徽章图标URL")
    private String iconUrl;

    @Schema(description = "达成条件描述")
    private String conditionDesc;
}
