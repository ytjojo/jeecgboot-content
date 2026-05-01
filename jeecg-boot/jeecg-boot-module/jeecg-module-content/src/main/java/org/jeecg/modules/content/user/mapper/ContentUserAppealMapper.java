package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserAppeal;

import java.util.List;

/**
 * Mapper for content user appeal.
 */
public interface ContentUserAppealMapper extends BaseMapper<ContentUserAppeal> {

    @Select("select * from content_user_appeal where user_id = #{userId} order by create_time desc")
    List<ContentUserAppeal> selectByUserId(String userId);
}
