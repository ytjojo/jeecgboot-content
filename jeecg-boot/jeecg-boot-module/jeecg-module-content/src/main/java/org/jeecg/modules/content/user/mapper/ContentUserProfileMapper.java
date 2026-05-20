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
}
