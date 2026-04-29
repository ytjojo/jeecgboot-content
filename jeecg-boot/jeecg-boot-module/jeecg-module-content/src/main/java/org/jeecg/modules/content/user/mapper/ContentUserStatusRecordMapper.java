package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;

public interface ContentUserStatusRecordMapper extends BaseMapper<ContentUserStatusRecord> {

    @Select("select * from content_user_status_record where user_id = #{userId} order by create_time desc limit 1")
    ContentUserStatusRecord selectLatestByUserId(@Param("userId") String userId);
}
