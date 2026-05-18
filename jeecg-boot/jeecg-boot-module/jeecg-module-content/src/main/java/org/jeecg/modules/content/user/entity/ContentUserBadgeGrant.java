package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 内容社区用户勋章授予实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_badge_grant")
@Schema(description = "内容社区用户勋章授予")
public class ContentUserBadgeGrant extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "勋章定义ID")
    private String badgeDefinitionId;

    @Schema(description = "勋章编码")
    private String badgeCode;

    @Schema(description = "发放来源")
    private String grantSource;

    @Schema(description = "发放原因")
    private String grantReason;

    @Schema(description = "是否展示中")
    private Boolean displaying;

    @Schema(description = "佩戴展示排序")
    private Integer displayOrder;

    @Schema(description = "勋章状态")
    private String status;

    @Schema(description = "到期时间")
    private Date expiresAt;

    @Schema(description = "回收操作人")
    private String recycledBy;

    @Schema(description = "回收原因")
    private String recycleReason;

    @Schema(description = "回收时间")
    private Date recycledAt;
}
