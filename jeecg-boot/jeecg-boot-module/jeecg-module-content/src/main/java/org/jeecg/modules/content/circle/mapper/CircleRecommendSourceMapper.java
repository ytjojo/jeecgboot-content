package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.content.circle.entity.CircleRecommendSource;

@Mapper
public interface CircleRecommendSourceMapper extends BaseMapper<CircleRecommendSource> {

    @Update("UPDATE circle_recommend_source SET click_time = NOW() WHERE id = #{id} AND click_time IS NULL")
    int updateClickTime(@Param("id") String id);

    @Update("UPDATE circle_recommend_source SET join_time = NOW() WHERE id = #{id} AND join_time IS NULL")
    int updateJoinTime(@Param("id") String id);
}
