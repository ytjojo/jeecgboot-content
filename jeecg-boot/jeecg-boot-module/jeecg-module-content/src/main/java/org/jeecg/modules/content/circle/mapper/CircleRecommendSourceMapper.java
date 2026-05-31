package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.content.circle.entity.CircleRecommendSource;

import java.util.List;

@Mapper
public interface CircleRecommendSourceMapper extends BaseMapper<CircleRecommendSource> {

    @Update("UPDATE circle_recommend_source SET click_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND click_time IS NULL")
    int updateClickTime(@Param("id") String id, @Param("userId") String userId);

    @Update("UPDATE circle_recommend_source SET join_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND join_time IS NULL")
    int updateJoinTime(@Param("id") String id, @Param("userId") String userId);

    /**
     * 批量插入推荐来源记录
     */
    default void insertBatch(List<CircleRecommendSource> sources) {
        for (CircleRecommendSource source : sources) {
            insert(source);
        }
    }

    /**
     * 单条插入（供批量方法调用）
     */
    @org.apache.ibatis.annotations.Insert("INSERT INTO circle_recommend_source (id, circle_id, user_id, source_type, create_time) VALUES (#{id, jdbcType=VARCHAR}, #{circleId}, #{userId}, #{sourceType}, NOW())")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CircleRecommendSource source);
}
