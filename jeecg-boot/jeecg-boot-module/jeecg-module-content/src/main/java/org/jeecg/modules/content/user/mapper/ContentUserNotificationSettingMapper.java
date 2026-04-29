package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;

/**
 * Mapper for content user notification setting.
 */
public interface ContentUserNotificationSettingMapper extends BaseMapper<ContentUserNotificationSetting> {

    @Select("select * from content_user_notification_setting where user_id = #{userId} limit 1")
    ContentUserNotificationSetting selectByUserId(@Param("userId") String userId);
}
