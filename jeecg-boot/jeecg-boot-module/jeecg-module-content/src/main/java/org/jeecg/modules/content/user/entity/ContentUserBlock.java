package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 内容社区用户拉黑关系。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_block")
@Schema(description = "内容社区用户拉黑关系")
public class ContentUserBlock extends JeecgEntity {

    @Schema(description = "拉黑发起用户ID")
    private String userId;

    @Schema(description = "被拉黑用户ID")
    private String blockedUserId;

    @Schema(description = "拉黑时间")
    private Date blockTime;

    @Schema(description = "拉黑状态")
    private String status;

    @Schema(description = "拉黑原因")
    private String reason;
}
