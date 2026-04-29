package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserRelation;

import java.util.List;

/**
 * Mapper for content user relation.
 */
public interface ContentUserRelationMapper extends BaseMapper<ContentUserRelation> {

    @Select("select * from content_user_relation where owner_user_id = #{ownerUserId} and target_user_id = #{targetUserId} limit 1")
    ContentUserRelation selectByPair(@Param("ownerUserId") String ownerUserId, @Param("targetUserId") String targetUserId);

    @Select("select * from content_user_relation where target_user_id = #{userId} and followed = 1")
    List<ContentUserRelation> selectFollowers(@Param("userId") String userId);
}
