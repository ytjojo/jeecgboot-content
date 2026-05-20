package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.content.user.entity.ContentUserProfile;

/**
 * Mapper for content user profile.
 */
public interface ContentUserProfileMapper extends BaseMapper<ContentUserProfile> {

    @Select("select * from content_user_profile where user_id = #{userId} limit 1")
    ContentUserProfile selectByUserId(@Param("userId") String userId);

    /**
     * 在余额足够时原子扣减积分，避免并发消费导致透支。
     */
    @Update("update content_user_profile set point_balance = point_balance - #{pointCost}, update_time = now() "
        + "where user_id = #{userId} and point_balance >= #{pointCost}")
    int deductPointIfEnough(@Param("userId") String userId, @Param("pointCost") int pointCost);

    /**
     * 按真实关系状态变化同步关注数，使用 greatest 防止异常回放造成负数。
     */
    @Update("update content_user_profile set following_count = greatest(0, following_count + #{delta}), update_time = now() "
        + "where user_id = #{userId}")
    int changeFollowingCount(@Param("userId") String userId, @Param("delta") int delta);

    /**
     * 按真实关系状态变化同步粉丝数，使用 greatest 防止异常回放造成负数。
     */
    @Update("update content_user_profile set follower_count = greatest(0, follower_count + #{delta}), update_time = now() "
        + "where user_id = #{userId}")
    int changeFollowerCount(@Param("userId") String userId, @Param("delta") int delta);

    /**
     * 按真实关系状态变化同步特别关注数，使用 greatest 防止异常回放造成负数。
     */
    @Update("update content_user_profile set special_follow_count = greatest(0, special_follow_count + #{delta}), update_time = now() "
        + "where user_id = #{userId}")
    int changeSpecialFollowCount(@Param("userId") String userId, @Param("delta") int delta);
}
