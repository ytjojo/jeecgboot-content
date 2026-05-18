package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserFeedSetting;

/**
 * 内容社区关注流设置 Mapper。
 */
public interface ContentUserFeedSettingMapper extends BaseMapper<ContentUserFeedSetting> {

    @Select("select * from content_user_feed_setting where user_id = #{userId} limit 1")
    ContentUserFeedSetting selectByUserId(@Param("userId") String userId);
}
