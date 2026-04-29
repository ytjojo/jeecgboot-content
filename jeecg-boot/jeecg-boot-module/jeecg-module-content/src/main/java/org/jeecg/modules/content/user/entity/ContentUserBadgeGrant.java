package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_badge_grant")
public class ContentUserBadgeGrant extends JeecgEntity {

    private String userId;
    private String badgeDefinitionId;
    private String badgeCode;
    private String grantSource;
    private String grantReason;
    private Boolean displaying;
    private String status;
    private Date expiresAt;
    private Date recycledAt;
}
