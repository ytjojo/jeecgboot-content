package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 内容社区邀请记录实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_invite_record")
@Schema(description = "内容社区邀请记录")
public class ContentInviteRecord extends JeecgEntity {

    @Schema(description = "邀请人用户ID")
    private String inviterUserId;

    @Schema(description = "被邀请人用户ID")
    private String inviteeUserId;

    @Schema(description = "邀请码")
    private String inviteCode;

    @Schema(description = "注册时间")
    private Date registeredAt;

    @Schema(description = "奖励积分")
    private Integer rewardPoint;

    @Schema(description = "奖励状态：PENDING/GRANTED")
    private String rewardStatus;
}
