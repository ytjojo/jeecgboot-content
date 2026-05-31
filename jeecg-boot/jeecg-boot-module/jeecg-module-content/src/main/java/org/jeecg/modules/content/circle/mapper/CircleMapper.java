package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.content.circle.entity.Circle;

@Mapper
public interface CircleMapper extends BaseMapper<Circle> {

    @Update("UPDATE content_circle SET member_count = member_count + 1, update_time = NOW() WHERE id = #{circleId} AND member_count < max_member_count")
    int incrementMemberCount(@Param("circleId") String circleId);

    @Update("UPDATE content_circle SET member_count = GREATEST(member_count - 1, 0), update_time = NOW() WHERE id = #{circleId}")
    int decrementMemberCount(@Param("circleId") String circleId);
}
