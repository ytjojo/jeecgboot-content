package org.jeecg.modules.content.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.auth.entity.ContentUserAccount;

/**
 * Mapper for content user account.
 */
public interface ContentUserAccountMapper extends BaseMapper<ContentUserAccount> {

    /**
     * 查询活跃状态的用户账号。
     */
    @Select("select * from content_user_account where user_id = #{userId} and account_status = 'ACTIVE'")
    ContentUserAccount selectActiveByUserId(@Param("userId") String userId);
}
