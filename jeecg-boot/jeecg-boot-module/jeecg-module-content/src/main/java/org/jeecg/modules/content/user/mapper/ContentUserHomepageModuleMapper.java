package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserHomepageModule;

import java.util.List;

/**
 * 内容社区用户主页模块 Mapper。
 */
public interface ContentUserHomepageModuleMapper extends BaseMapper<ContentUserHomepageModule> {

    @Select("select * from content_user_homepage_module where user_id = #{userId} order by sort_order asc")
    List<ContentUserHomepageModule> selectByUserId(@Param("userId") String userId);
}
