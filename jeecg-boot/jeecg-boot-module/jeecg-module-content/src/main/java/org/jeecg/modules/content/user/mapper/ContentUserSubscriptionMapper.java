package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserSubscription;

import java.util.List;

/**
 * Mapper for content user subscription.
 */
public interface ContentUserSubscriptionMapper extends BaseMapper<ContentUserSubscription> {

    @Select("select * from content_user_subscription where user_id = #{userId} and source_type = #{sourceType} and source_id = #{sourceId} limit 1")
    ContentUserSubscription selectByUniqueKey(@Param("userId") String userId,
                                              @Param("sourceType") String sourceType,
                                              @Param("sourceId") String sourceId);

    @Select("select * from content_user_subscription where user_id = #{userId}")
    List<ContentUserSubscription> selectByUserId(@Param("userId") String userId);

    @Select("select count(1) from content_user_subscription where user_id = #{userId} and source_type = #{sourceType}")
    Long countByUserIdAndSourceType(@Param("userId") String userId, @Param("sourceType") String sourceType);
}
