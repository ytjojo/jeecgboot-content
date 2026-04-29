package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserProfile;

public interface ContentUserProfileMapper extends BaseMapper<ContentUserProfile> {

    @Select("select * from content_user_profile where user_id = #{userId} limit 1")
    ContentUserProfile selectByUserId(@Param("userId") String userId);
}
