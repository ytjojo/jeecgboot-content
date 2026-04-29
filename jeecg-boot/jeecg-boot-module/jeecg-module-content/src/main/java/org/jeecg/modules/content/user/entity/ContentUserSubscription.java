package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_subscription")
public class ContentUserSubscription extends JeecgEntity {

    private String userId;
    private String sourceType;
    private String sourceId;
    private String sourceName;
    private String notificationChannels;
    private String notificationFrequency;
    private Boolean paused;
    private String recommendationReason;
}
