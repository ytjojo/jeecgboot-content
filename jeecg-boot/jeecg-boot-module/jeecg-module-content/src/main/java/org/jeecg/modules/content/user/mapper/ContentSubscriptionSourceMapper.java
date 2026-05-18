package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentSubscriptionSource;

/**
 * 内容社区订阅源目录 Mapper。
 */
public interface ContentSubscriptionSourceMapper extends BaseMapper<ContentSubscriptionSource> {

    @Select("select * from content_subscription_source where source_type = #{sourceType} and source_id = #{sourceId} limit 1")
    ContentSubscriptionSource selectBySource(@Param("sourceType") String sourceType, @Param("sourceId") String sourceId);
}
