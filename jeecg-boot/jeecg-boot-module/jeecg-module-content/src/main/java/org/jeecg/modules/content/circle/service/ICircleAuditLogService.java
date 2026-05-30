package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.entity.CircleAuditLog;

import java.util.Date;
import java.util.List;

/**
 * 圈子审核日志服务接口。
 * 提供审核日志写入和查询功能。
 */
public interface ICircleAuditLogService {

    /**
     * 写入审核日志，createdAt 为空时自动设置为当前时间
     *
     * @param log 审核日志对象
     */
    void writeAuditLog(CircleAuditLog log);

    /**
     * 根据目标ID和目标类型查询审核日志
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 审核日志列表
     */
    List<CircleAuditLog> queryByTarget(String targetId, String targetType);

    /**
     * 根据时间范围查询审核日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 审核日志列表
     */
    List<CircleAuditLog> queryByTimeRange(Date startTime, Date endTime);
}
