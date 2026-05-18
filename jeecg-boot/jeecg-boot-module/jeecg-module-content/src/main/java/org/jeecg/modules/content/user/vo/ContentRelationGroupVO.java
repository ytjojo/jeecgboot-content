package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserRelationGroup;

/**
 * 内容社区关注分组视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区关注分组视图")
public class ContentRelationGroupVO {

    @Schema(description = "分组ID")
    private String groupId;

    @Schema(description = "拥有者用户ID")
    private String ownerUserId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "是否默认分组")
    private Boolean isDefault;

    @Schema(description = "分组状态")
    private String groupStatus;

    /**
     * 从分组实体构建视图对象。
     */
    public static ContentRelationGroupVO from(ContentUserRelationGroup group) {
        return new ContentRelationGroupVO()
            .setGroupId(group.getId())
            .setOwnerUserId(group.getOwnerUserId())
            .setGroupName(group.getGroupName())
            .setSortOrder(group.getSortOrder())
            .setIsDefault(group.getIsDefault())
            .setGroupStatus(group.getGroupStatus());
    }
}
