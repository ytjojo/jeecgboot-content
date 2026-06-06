package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.content.circle.entity.CircleRecommendSource;

import java.util.List;

@Mapper
public interface CircleRecommendSourceMapper extends BaseMapper<CircleRecommendSource> {

    @Update("UPDATE circle_recommend_source SET exposure_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND exposure_time IS NULL")
    int updateExposureTime(@Param("id") String id, @Param("userId") String userId);

    @Update("UPDATE circle_recommend_source SET click_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND click_time IS NULL")
    int updateClickTime(@Param("id") String id, @Param("userId") String userId);

    @Update("UPDATE circle_recommend_source SET join_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND join_time IS NULL")
    int updateJoinTime(@Param("id") String id, @Param("userId") String userId);

    /**
     * 真正的批量插入（XML foreach，单条SQL）
     */
    int insertBatch(@Param("list") List<CircleRecommendSource> sources);
}
