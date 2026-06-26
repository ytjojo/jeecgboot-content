package org.jeecg.modules.content.circle.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 圈子邀请记录实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_invite_record")
@Schema(description = "圈子邀请记录")
public class CircleInviteRecord extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "邀请人用户ID")
    private String inviterId;

    @Schema(description = "被邀请人用户ID")
    private String inviteeId;

    @Schema(description = "邀请状态: PENDING/JOINED/EXPIRED")
    private String status;
}
