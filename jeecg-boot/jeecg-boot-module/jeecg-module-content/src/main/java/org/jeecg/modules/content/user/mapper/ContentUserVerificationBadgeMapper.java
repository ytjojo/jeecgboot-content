package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserVerificationBadge;

import java.util.List;

/**
 * 内容社区用户认证标识 Mapper。
 */
public interface ContentUserVerificationBadgeMapper extends BaseMapper<ContentUserVerificationBadge> {

    @Select("select * from content_user_verification_badge where user_id = #{userId} and status = 'ACTIVE' order by verified_at desc, create_time desc")
    List<ContentUserVerificationBadge> selectActiveByUserId(@Param("userId") String userId);
}
