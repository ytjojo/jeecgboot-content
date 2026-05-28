package org.jeecg.modules.content.userstatus.service;

import org.jeecg.modules.content.userstatus.entity.UserStatusAuditLog;

import java.util.Date;
import java.util.List;

/**
 * 审计日志服务接口。
 * 提供审计日志写入、查询、导出功能。
 */
public interface UserStatusAuditLogService {

    /**
     * 写入审计日志
     *
     * @param auditLog 审计日志对象
     */
    void writeAuditLog(UserStatusAuditLog auditLog);

    /**
     * 根据用户ID查询审计日志
     *
     * @param userId 用户ID
     * @return 审计日志列表
     */
    List<UserStatusAuditLog> queryByUserId(String userId);

    /**
     * 根据用户ID和时间范围查询审计日志
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 审计日志列表
     */
    List<UserStatusAuditLog> queryByUserIdAndTimeRange(String userId, Date startTime, Date endTime);
}
