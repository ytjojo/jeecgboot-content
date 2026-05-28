package org.jeecg.modules.content.userstatus.biz;

import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserStatusRecordMapper;
import org.jeecg.modules.content.userstatus.entity.UserStatusAuditLog;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.jeecg.modules.content.userstatus.service.UserStatusAuditLogService;
import org.jeecg.modules.content.userstatus.service.UserStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 状态变更编排服务。
 * 编排状态变更和审计日志写入，保证事务一致性。
 */
@Service
public class UserStatusBizManageService {

    @Resource
    private UserStatusService userStatusService;

    @Resource
    private UserStatusAuditLogService auditLogService;

    @Resource
    private ContentUserProfileMapper userProfileMapper;

    @Resource
    private ContentUserStatusRecordMapper statusRecordMapper;

    /**
     * 执行状态变更（含审计日志写入）
     *
     * @param userId       用户ID
     * @param fromStatus   原状态
     * @param toStatus     目标状态
     * @param reason       变更原因
     * @param operatorId   操作人ID
     * @param operatorType 操作人类型（SYSTEM/ADMIN）
     * @param ipAddress    操作人IP地址
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(String userId, UserStatusEnum fromStatus, UserStatusEnum toStatus,
                             String reason, String operatorId, String operatorType, String ipAddress) {
        // 1. 验证状态变更是否合法
        userStatusService.validateStatusChange(fromStatus, toStatus, reason, false);

        // 2. 更新用户资料表状态
        ContentUserProfile profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new org.jeecg.common.exception.JeecgBootException("用户资料不存在: " + userId);
        }
        profile.setStatus(toStatus.name());
        userProfileMapper.updateById(profile);

        // 3. 写入状态变更记录
        ContentUserStatusRecord record = new ContentUserStatusRecord();
        record.setUserId(userId);
        record.setCurrentStatus(fromStatus.name());
        record.setTargetStatus(toStatus.name());
        record.setTriggerSource("ADMIN".equals(operatorType) ? "MANUAL" : "AUTO");
        record.setOperatorUserId(operatorId);
        record.setReason(reason);
        record.setEffectiveStartTime(new Date());
        record.setRecoverable(true);
        statusRecordMapper.insert(record);

        // 4. 写入审计日志
        UserStatusAuditLog auditLog = new UserStatusAuditLog();
        auditLog.setUserId(userId);
        auditLog.setFromStatus(fromStatus.name());
        auditLog.setToStatus(toStatus.name());
        auditLog.setOperatorId(operatorId);
        auditLog.setOperatorType(operatorType);
        auditLog.setTriggerReason(reason);
        auditLog.setStartTime(new Date());
        auditLog.setIpAddress(ipAddress);
        auditLog.setCreatedAt(new Date());

        auditLogService.writeAuditLog(auditLog);
    }

    /**
     * 执行管理员强制状态变更（含审计日志写入）
     *
     * @param userId       用户ID
     * @param fromStatus   原状态
     * @param toStatus     目标状态
     * @param reason       变更原因
     * @param operatorId   操作人ID
     * @param operatorType 操作人类型（SYSTEM/ADMIN）
     * @param ipAddress    操作人IP地址
     */
    @Transactional(rollbackFor = Exception.class)
    public void forceChangeStatus(String userId, UserStatusEnum fromStatus, UserStatusEnum toStatus,
                                  String reason, String operatorId, String operatorType, String ipAddress) {
        // 1. 验证状态变更是否合法（管理员强制转换）
        userStatusService.validateStatusChange(fromStatus, toStatus, reason, true);

        // 2. 更新用户资料表状态
        ContentUserProfile profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new org.jeecg.common.exception.JeecgBootException("用户资料不存在: " + userId);
        }
        profile.setStatus(toStatus.name());
        userProfileMapper.updateById(profile);

        // 3. 写入状态变更记录
        ContentUserStatusRecord record = new ContentUserStatusRecord();
        record.setUserId(userId);
        record.setCurrentStatus(fromStatus.name());
        record.setTargetStatus(toStatus.name());
        record.setTriggerSource("MANUAL");
        record.setOperatorUserId(operatorId);
        record.setReason(reason);
        record.setEffectiveStartTime(new Date());
        record.setRecoverable(true);
        statusRecordMapper.insert(record);

        // 4. 写入审计日志
        UserStatusAuditLog auditLog = new UserStatusAuditLog();
        auditLog.setUserId(userId);
        auditLog.setFromStatus(fromStatus.name());
        auditLog.setToStatus(toStatus.name());
        auditLog.setOperatorId(operatorId);
        auditLog.setOperatorType(operatorType);
        auditLog.setTriggerReason(reason);
        auditLog.setStartTime(new Date());
        auditLog.setIpAddress(ipAddress);
        auditLog.setCreatedAt(new Date());

        auditLogService.writeAuditLog(auditLog);
    }

    /**
     * 查找到期状态用户
     *
     * @param status 用户状态
     * @return 到期用户ID列表
     */
    public List<String> findExpiredStatusUsers(UserStatusEnum status) {
        // TODO: 实现查询到期用户逻辑
        return List.of();
    }

    /**
     * 批量变更用户状态
     *
     * @param userIds      用户ID列表
     * @param fromStatus   原状态
     * @param toStatus     目标状态
     * @param reason       变更原因
     * @param operatorId   操作人ID
     * @param operatorType 操作人类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchChangeStatus(List<String> userIds, UserStatusEnum fromStatus, UserStatusEnum toStatus,
                                  String reason, String operatorId, String operatorType) {
        for (String userId : userIds) {
            changeStatus(userId, fromStatus, toStatus, reason, operatorId, operatorType, null);
        }
    }
}
