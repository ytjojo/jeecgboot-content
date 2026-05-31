package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.content.circle.entity.Circle;

import java.util.List;

@Mapper
public interface CircleMapper extends BaseMapper<Circle> {

    @Update("UPDATE content_circle SET member_count = member_count + 1, update_time = NOW() WHERE id = #{circleId} AND member_count < max_member_count")
    int incrementMemberCount(@Param("circleId") String circleId);

    @Update("UPDATE content_circle SET member_count = GREATEST(member_count - 1, 0), update_time = NOW() WHERE id = #{circleId}")
    int decrementMemberCount(@Param("circleId") String circleId);

    /**
     * 查询热门圈子（按成员数降序）
     */
    @Select("SELECT * FROM content_circle WHERE status = 'ACTIVE' AND privacy_type = 'PUBLIC' ORDER BY member_count DESC LIMIT #{limit}")
    List<Circle> selectHotCircles(@Param("limit") int limit);

    /**
     * 查询新增圈子（按创建时间降序）
     */
    @Select("SELECT * FROM content_circle WHERE status = 'ACTIVE' AND privacy_type = 'PUBLIC' ORDER BY create_time DESC LIMIT #{limit}")
    List<Circle> selectNewCircles(@Param("limit") int limit);

    /**
     * 查询推荐候选圈子（排除用户已加入的圈子，按成员数降序）
     */
    @Select("SELECT * FROM content_circle WHERE status = 'ACTIVE' AND privacy_type = 'PUBLIC' AND id NOT IN (SELECT circle_id FROM content_circle_member WHERE user_id = #{userId} AND status = 'ACTIVE') ORDER BY member_count DESC LIMIT #{limit}")
    List<Circle> selectRecommendCandidates(@Param("userId") String userId, @Param("limit") int limit);
}
