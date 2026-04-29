package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserPrivacySetting;

public interface ContentUserPrivacySettingMapper extends BaseMapper<ContentUserPrivacySetting> {

    @Select("select * from content_user_privacy_setting where user_id = #{userId} limit 1")
    ContentUserPrivacySetting selectByUserId(@Param("userId") String userId);
}
