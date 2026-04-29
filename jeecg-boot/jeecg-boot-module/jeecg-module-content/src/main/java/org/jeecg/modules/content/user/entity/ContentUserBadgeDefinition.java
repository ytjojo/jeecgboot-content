package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_badge_definition")
public class ContentUserBadgeDefinition extends JeecgEntity {

    private String badgeCode;
    private String badgeName;
    private String badgeType;
    private String ruleConfigJson;
    private Integer validDays;
    private Boolean autoGrant;
    private Boolean enabled;
}
