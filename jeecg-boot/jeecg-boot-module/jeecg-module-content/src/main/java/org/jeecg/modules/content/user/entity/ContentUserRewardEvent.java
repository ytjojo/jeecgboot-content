package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区用户奖励事件实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_reward_event")
@Schema(description = "内容社区用户奖励事件")
public class ContentUserRewardEvent extends JeecgEntity {

    @Schema(description = "奖励事件ID")
    private String eventId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "行为来源类型")
    private String sourceType;

    @Schema(description = "命中奖励规则编码")
    private String ruleCode;

    @Schema(description = "实际奖励积分")
    private Integer pointDelta;

    @Schema(description = "实际奖励成长值")
    private Integer growthDelta;

    @Schema(description = "每日统计桶")
    private String dailyBucket;

    @Schema(description = "处理状态")
    private String processStatus;

    @Schema(description = "跳过原因")
    private String skipReason;
}
