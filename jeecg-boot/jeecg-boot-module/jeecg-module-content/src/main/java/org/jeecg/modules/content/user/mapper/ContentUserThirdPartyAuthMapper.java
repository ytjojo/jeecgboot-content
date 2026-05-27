package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.content.user.entity.ContentUserThirdPartyAuth;

import java.util.Date;
import java.util.List;

/**
 * 内容社区用户第三方授权 Mapper。
 */
public interface ContentUserThirdPartyAuthMapper extends BaseMapper<ContentUserThirdPartyAuth> {

    /**
     * 查询指定用户的所有活跃授权记录。
     */
    @Select("SELECT * FROM content_user_third_party_auth WHERE user_id = #{userId} AND status = 'ACTIVE' ORDER BY auth_time DESC")
    List<ContentUserThirdPartyAuth> selectActiveByUserId(@Param("userId") String userId);

    /**
     * 查询指定用户的单条授权记录（不限状态，用于撤销校验）。
     */
    @Select("SELECT * FROM content_user_third_party_auth WHERE id = #{authId} AND user_id = #{userId} LIMIT 1")
    ContentUserThirdPartyAuth selectByAuthIdAndUserId(@Param("authId") String authId, @Param("userId") String userId);

    /**
     * 撤销授权：标记状态为 REVOKED，清除 token hash。
     */
    @Update("UPDATE content_user_third_party_auth SET status = 'REVOKED', token_hash = NULL, refresh_token_hash = NULL, revoked_at = #{revokedAt} WHERE id = #{authId} AND user_id = #{userId} AND status = 'ACTIVE'")
    int revokeByAuthIdAndUserId(@Param("authId") String authId, @Param("userId") String userId, @Param("revokedAt") Date revokedAt);
}
