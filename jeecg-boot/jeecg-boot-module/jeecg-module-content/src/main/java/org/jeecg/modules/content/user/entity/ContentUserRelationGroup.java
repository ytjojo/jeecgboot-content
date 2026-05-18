package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * Entity for content user relation group.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_relation_group")
public class ContentUserRelationGroup extends JeecgEntity {

    @Schema(description = "分组所属用户ID")
    private String ownerUserId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "是否默认分组")
    private Boolean isDefault;

    @Schema(description = "分组状态")
    private String groupStatus;
}
