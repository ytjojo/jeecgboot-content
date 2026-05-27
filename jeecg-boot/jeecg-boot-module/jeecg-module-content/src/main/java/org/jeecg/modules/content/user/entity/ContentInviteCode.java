package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区邀请码实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_invite_code")
@Schema(description = "内容社区邀请码")
public class ContentInviteCode extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "邀请码（8位字母数字）")
    private String inviteCode;
}
