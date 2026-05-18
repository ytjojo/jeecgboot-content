package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 内容社区成长值衰减状态实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_growth_decay_state")
@Schema(description = "内容社区成长值衰减状态")
public class ContentUserGrowthDecayState extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "最后活跃时间")
    private Date lastActiveTime;

    @Schema(description = "衰减次数")
    private Integer decayCount;

    @Schema(description = "最后衰减时间")
    private Date lastDecayTime;

    @Schema(description = "降级保护开始时间")
    private Date protectionStartedAt;

    @Schema(description = "降级保护结束时间")
    private Date protectionUntil;

    @Schema(description = "衰减状态")
    private String status;

    @Schema(description = "衰减规则快照JSON")
    private String ruleSnapshotJson;
}
