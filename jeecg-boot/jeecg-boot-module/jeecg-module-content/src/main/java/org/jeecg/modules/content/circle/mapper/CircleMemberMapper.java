package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.circle.entity.CircleMember;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 圈子成员 Mapper 接口。
 */
@Mapper
public interface CircleMemberMapper extends BaseMapper<CircleMember> {

    /**
     * 查询圈子所有活跃成员的用户ID。
     *
     * @param circleId 圈子ID
     * @return 活跃成员用户ID列表
     */
    List<String> selectMemberUserIds(@Param("circleId") String circleId);

    /**
     * 查询指定用户ID中属于圈子活跃成员的用户ID。
     *
     * @param circleId 圈子ID
     * @param userIds  待筛选的用户ID集合
     * @return 属于活跃成员的用户ID列表
     */
    List<String> selectActiveMemberUserIds(@Param("circleId") String circleId,
                                           @Param("userIds") Collection<String> userIds);

    /**
     * 查询圈子活跃成员中用户ID包含关键字的用户ID。
     *
     * @param circleId 圈子ID
     * @param keyword  关键字（模糊匹配用户ID）
     * @return 匹配的用户ID列表
     */
    List<String> selectMemberUserIdsByKeyword(@Param("circleId") String circleId,
                                              @Param("keyword") String keyword);

    /**
     * 查询用户加入的所有活跃圈子ID
     */
    @Select("SELECT circle_id FROM circle_member WHERE user_id = #{userId} AND status = 'ACTIVE'")
    List<String> selectCircleIdsByUserId(@Param("userId") String userId);

    /**
     * 聚合查询所有圈子的成员统计（单条SQL替代N+1循环）
     * 返回 List of Map: {circle_id, member_count, new_member_count}
     */
    List<Map<String, Object>> selectMemberStatsGroupByCircle(@Param("todayStart") LocalDateTime todayStart);

}
