package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserMute;

/**
 * 内容社区用户屏蔽关系 Mapper。
 */
public interface ContentUserMuteMapper extends BaseMapper<ContentUserMute> {

    @Select("select * from content_user_mute where user_id = #{userId} and muted_user_id = #{mutedUserId} limit 1")
    ContentUserMute selectByPair(@Param("userId") String userId, @Param("mutedUserId") String mutedUserId);
}
