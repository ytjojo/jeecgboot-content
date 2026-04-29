package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_relation_group")
public class ContentUserRelationGroup extends JeecgEntity {

    private String ownerUserId;
    private String groupName;
    private Integer sortOrder;
    private Boolean isDefault;
}
