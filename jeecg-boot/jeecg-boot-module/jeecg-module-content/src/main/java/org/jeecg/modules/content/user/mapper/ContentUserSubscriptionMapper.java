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

    @Select("select * from content_user_subscription where user_id = #{userId}")
    List<ContentUserSubscription> selectByUserId(@Param("userId") String userId);
}
