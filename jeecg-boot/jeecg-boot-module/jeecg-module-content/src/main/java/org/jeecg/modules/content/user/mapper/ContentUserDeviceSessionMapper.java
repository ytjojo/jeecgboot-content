package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;

import java.util.Date;

/**
 * Mapper for content user device session.
 */
public interface ContentUserDeviceSessionMapper extends BaseMapper<ContentUserDeviceSession> {

    /**
     * 查询用户设备会话中的最近活跃时间。
     */
    @Select("select max(last_active_time) from content_user_device_session where user_id = #{userId}")
    Date selectLatestActiveTimeByUserId(@Param("userId") String userId);
}
