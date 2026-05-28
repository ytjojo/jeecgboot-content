package org.jeecg.modules.content.userstatus.service.impl;

import org.jeecg.modules.content.userstatus.entity.UserStatusAuditLog;
import org.jeecg.modules.content.userstatus.mapper.UserStatusAuditLogMapper;
import org.jeecg.modules.content.userstatus.service.UserStatusAuditLogService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 审计日志服务实现。
 * 提供审计日志写入、查询、导出功能。
 */
@Service
public class UserStatusAuditLogServiceImpl implements UserStatusAuditLogService {

    @Resource
    private UserStatusAuditLogMapper auditLogMapper;

    @Override
    public void writeAuditLog(UserStatusAuditLog auditLog) {
        if (auditLog.getCreatedAt() == null) {
            auditLog.setCreatedAt(new Date());
        }
        auditLogMapper.insert(auditLog);
    }

    @Override
    public List<UserStatusAuditLog> queryByUserId(String userId) {
        return auditLogMapper.selectByUserId(userId);
    }

    @Override
    public List<UserStatusAuditLog> queryByUserIdAndTimeRange(String userId, Date startTime, Date endTime) {
        return auditLogMapper.selectByUserIdAndTimeRange(userId, startTime, endTime);
    }
}
