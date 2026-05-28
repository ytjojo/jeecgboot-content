package org.jeecg.modules.content.userstatus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.userstatus.entity.UserStatusAuditLog;

import java.util.Date;
import java.util.List;

/**
 * 审计日志 Mapper 接口。
 */
@Mapper
public interface UserStatusAuditLogMapper extends BaseMapper<UserStatusAuditLog> {

    /**
     * 根据用户ID查询审计日志
     *
     * @param userId 用户ID
     * @return 审计日志列表
     */
    List<UserStatusAuditLog> selectByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和时间范围查询审计日志
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 审计日志列表
     */
    List<UserStatusAuditLog> selectByUserIdAndTimeRange(
        @Param("userId") String userId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );
}
