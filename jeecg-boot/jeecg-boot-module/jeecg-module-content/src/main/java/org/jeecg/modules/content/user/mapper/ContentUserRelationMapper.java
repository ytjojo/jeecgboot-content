package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserRelation;

import java.util.Collection;
import java.util.List;

/**
 * Mapper for content user relation.
 */
public interface ContentUserRelationMapper extends BaseMapper<ContentUserRelation> {

    @Select("select * from content_user_relation where owner_user_id = #{ownerUserId} and target_user_id = #{targetUserId} limit 1")
    ContentUserRelation selectByPair(@Param("ownerUserId") String ownerUserId, @Param("targetUserId") String targetUserId);

    @Select("select * from content_user_relation where target_user_id = #{userId} and followed = 1")
    List<ContentUserRelation> selectFollowers(@Param("userId") String userId);

    /**
     * 查询指定用户已关注且对方也关注了自己的目标用户ID列表（互关）。
     */
    @Select("SELECT r1.target_user_id FROM content_user_relation r1 "
        + "INNER JOIN content_user_relation r2 "
        + "ON r1.target_user_id = r2.owner_user_id AND r2.target_user_id = r1.owner_user_id "
        + "WHERE r1.owner_user_id = #{ownerUserId} AND r1.followed = 1 AND r2.followed = 1 "
        + "AND r1.relation_status = 'ACTIVE'")
    List<String> selectMutualFollowIds(@Param("ownerUserId") String ownerUserId);

    /**
     * 批量查询指定owner对targets的关注关系中followed=1的target集合。
     */
    @Select("<script>SELECT target_user_id FROM content_user_relation "
        + "WHERE owner_user_id = #{ownerUserId} AND followed = 1 AND relation_status = 'ACTIVE' "
        + "AND target_user_id IN <foreach collection='targetIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<String> selectFollowedTargetIds(@Param("ownerUserId") String ownerUserId, @Param("targetIds") Collection<String> targetIds);
}
