package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserProfileHistory;

import java.util.List;

/**
 * 内容社区用户资料历史 Mapper。
 */
public interface ContentUserProfileHistoryMapper extends BaseMapper<ContentUserProfileHistory> {

    @Select("select * from content_user_profile_history where user_id = #{userId} and history_type = #{historyType} and expired = 0 order by create_time desc limit #{limit}")
    List<ContentUserProfileHistory> selectActiveByType(@Param("userId") String userId,
                                                       @Param("historyType") String historyType,
                                                       @Param("limit") int limit);
}
