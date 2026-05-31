package org.jeecg.modules.content.user.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 成员已获得徽章实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_member_achievement")
@Schema(description = "成员已获得徽章")
public class CircleMemberAchievement extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "徽章类型")
    private String achievementType;

    @Schema(description = "是否已撤销")
    private Boolean revoked;
}
