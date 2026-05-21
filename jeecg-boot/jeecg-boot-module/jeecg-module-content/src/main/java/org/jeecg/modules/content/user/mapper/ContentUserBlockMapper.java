package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserBlock;

/**
 * 内容社区用户拉黑关系 Mapper。
 */
public interface ContentUserBlockMapper extends BaseMapper<ContentUserBlock> {

    @Select("select * from content_user_block where user_id = #{userId} and blocked_user_id = #{blockedUserId} limit 1")
    ContentUserBlock selectByPair(@Param("userId") String userId, @Param("blockedUserId") String blockedUserId);
}
