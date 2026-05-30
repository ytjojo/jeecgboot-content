package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 圈子成员实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("circle_member")
@Schema(description = "圈子成员")
public class CircleMember extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "禁言截止时间")
    private Date mutedUntil;
}
