package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserProfileReview;

/**
 * 内容社区用户资料审核 Mapper。
 */
public interface ContentUserProfileReviewMapper extends BaseMapper<ContentUserProfileReview> {

    @Select("select * from content_user_profile_review where user_id = #{userId} and review_status = 'PENDING' order by create_time desc limit 1")
    ContentUserProfileReview selectPendingByUserId(@Param("userId") String userId);
}
