package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区用户奖励规则实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_reward_rule")
@Schema(description = "内容社区用户奖励规则")
public class ContentUserRewardRule extends JeecgEntity {

    @Schema(description = "奖励规则编码")
    private String ruleCode;

    @Schema(description = "行为来源类型")
    private String sourceType;

    @Schema(description = "奖励积分")
    private Integer pointAmount;

    @Schema(description = "奖励成长值")
    private Integer growthAmount;

    @Schema(description = "每日积分上限")
    private Integer dailyPointCap;

    @Schema(description = "每日成长值上限")
    private Integer dailyGrowthCap;

    @Schema(description = "规则说明")
    private String ruleDescription;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
