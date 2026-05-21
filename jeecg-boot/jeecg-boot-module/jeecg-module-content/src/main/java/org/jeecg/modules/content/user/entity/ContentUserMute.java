package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 内容社区用户屏蔽关系。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_mute")
@Schema(description = "内容社区用户屏蔽关系")
public class ContentUserMute extends JeecgEntity {

    @Schema(description = "屏蔽发起用户ID")
    private String userId;

    @Schema(description = "被屏蔽用户ID")
    private String mutedUserId;

    @Schema(description = "屏蔽时间")
    private Date muteTime;

    @Schema(description = "屏蔽状态")
    private String status;

    @Schema(description = "屏蔽原因")
    private String reason;
}
