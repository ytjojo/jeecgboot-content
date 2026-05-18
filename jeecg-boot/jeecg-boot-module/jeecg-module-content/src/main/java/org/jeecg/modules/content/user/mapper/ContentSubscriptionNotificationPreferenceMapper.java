package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentSubscriptionNotificationPreference;

/**
 * 内容社区订阅通知偏好 Mapper。
 */
public interface ContentSubscriptionNotificationPreferenceMapper extends BaseMapper<ContentSubscriptionNotificationPreference> {

    @Select("select * from content_subscription_notification_preference where subscription_id = #{subscriptionId} limit 1")
    ContentSubscriptionNotificationPreference selectBySubscriptionId(@Param("subscriptionId") String subscriptionId);
}
