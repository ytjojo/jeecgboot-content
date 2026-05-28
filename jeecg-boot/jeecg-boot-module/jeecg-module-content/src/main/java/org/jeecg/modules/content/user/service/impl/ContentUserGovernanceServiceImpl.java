package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.enums.ContentCommunityRoleEnum;
import org.jeecg.modules.content.user.enums.ContentUserStatusEnum;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserStatusRecordMapper;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.service.IContentUserGrowthPenaltyRecordService;
import org.jeecg.modules.content.user.service.IContentUserGrowthPenaltyRecoveryService;
import org.jeecg.modules.content.user.service.IContentUserGovernanceService;
import org.jeecg.modules.content.user.vo.ContentUserStatusHistoryItemVO;
import org.jeecg.modules.content.user.vo.ContentUserStatusHistoryPageVO;
import org.jeecg.modules.content.user.vo.ContentUserStatusVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for content user governance.
 */
@Service
public class ContentUserGovernanceServiceImpl implements IContentUserGovernanceService {

    private static final String AUTO_RECOVER_OPERATOR = "system";
    private static final String AUTO_RECOVER_TRIGGER = "AUTO_EXPIRE_RECOVER";
    private static final List<String> PENALTY_STATUSES = List.of(
        ContentUserStatusEnum.MUTED.getCode(),
        ContentUserStatusEnum.RECOMMENDATION_LIMITED.getCode(),
        ContentUserStatusEnum.FROZEN.getCode(),
        ContentUserStatusEnum.BANNED.getCode()
    );
    private static final List<String> AUTO_RECOVERABLE_STATUSES = List.of(
        ContentUserStatusEnum.MUTED.getCode(),
        ContentUserStatusEnum.RECOMMENDATION_LIMITED.getCode(),
        ContentUserStatusEnum.FROZEN.getCode(),
        ContentUserStatusEnum.BANNED.getCode()
    );

    @Resource
    private ContentUserStatusRecordMapper statusRecordMapper;

    @Resource
    private ContentUserAuditLogMapper auditLogMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserDeviceSessionMapper deviceSessionMapper;

    @Resource
    private IContentUserGrowthPenaltyRecoveryService growthPenaltyRecoveryService;

    @Resource
    private IContentUserGrowthPenaltyRecordService growthPenaltyRecordService;

    /**
     * Changes the lifecycle status of the target user and records governance logs.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(ContentUserStatusChangeReq req) {
        validateTransition(req);
        ContentUserStatusRecord record = ContentUserStatusRecord.from(req);
        statusRecordMapper.insert(record);
        updateProfileStatus(req.getUserId(), req.getTargetStatus());
        if (growthPenaltyRecordService != null && PENALTY_STATUSES.contains(req.getTargetStatus())) {
            growthPenaltyRecordService.createFromGovernanceRecord(record, req, new Date());
        }
        auditLogMapper.insert(ContentUserAuditLog.statusChange(req));
    }

    /**
     * Checks whether the user can execute the requested action.
     */
    @Override
    public boolean canExecuteAction(String userId, String actionType) {
        String currentStatus = resolveCurrentStatus(userId);
        String normalizedAction = actionType == null ? "" : actionType.toUpperCase(Locale.ROOT);
        if (ContentUserStatusEnum.FROZEN.getCode().equals(currentStatus)
            || ContentUserStatusEnum.BANNED.getCode().equals(currentStatus)
            || ContentUserStatusEnum.CANCEL_PENDING.getCode().equals(currentStatus)
            || ContentUserStatusEnum.CANCELLED.getCode().equals(currentStatus)) {
            return false;
        }
        if (ContentUserStatusEnum.MUTED.getCode().equals(currentStatus)) {
            return !("COMMENT".equals(normalizedAction)
                || "PRIVATE_MESSAGE".equals(normalizedAction)
                || "ANSWER".equals(normalizedAction)
                || "POST_DYNAMIC".equals(normalizedAction));
        }
        if (ContentUserStatusEnum.RECOMMENDATION_LIMITED.getCode().equals(currentStatus)) {
            return !"RECOMMEND".equals(normalizedAction);
        }
        return true;
    }

    /**
     * Gets the current lifecycle status snapshot for the target user.
     */
    @Override
    public ContentUserStatusVO getCurrentStatus(String userId) {
        ContentUserStatusRecord latestRecord = statusRecordMapper.selectLatestByUserId(userId);
        if (latestRecord != null) {
            return new ContentUserStatusVO()
                .setUserId(userId)
                .setCurrentStatus(latestRecord.getCurrentStatus())
                .setTargetStatus(latestRecord.getTargetStatus())
                .setReason(latestRecord.getReason())
                .setEffectiveStartTime(latestRecord.getEffectiveStartTime())
                .setEffectiveEndTime(latestRecord.getEffectiveEndTime());
        }
        ContentUserProfile profile = profileMapper == null ? null : profileMapper.selectByUserId(userId);
        return new ContentUserStatusVO()
            .setUserId(userId)
            .setCurrentStatus(profile == null ? ContentUserStatusEnum.GUEST.getCode() : profile.getStatus())
            .setTargetStatus(profile == null ? ContentUserStatusEnum.GUEST.getCode() : profile.getStatus());
    }

    /**
     * Queries paged status history for the target user.
     */
    @Override
    public ContentUserStatusHistoryPageVO listStatusHistory(String userId, Long pageNo, Long pageSize) {
        long currentPage = pageNo == null || pageNo < 1L ? 1L : pageNo;
        long currentSize = pageSize == null || pageSize < 1L ? 10L : pageSize;
        IPage<ContentUserStatusRecord> page = statusRecordMapper.selectPage(
            new Page<>(currentPage, currentSize),
            Wrappers.<ContentUserStatusRecord>lambdaQuery()
                .eq(ContentUserStatusRecord::getUserId, userId)
                .orderByDesc(ContentUserStatusRecord::getCreateTime)
        );
        return new ContentUserStatusHistoryPageVO()
            .setRecords(page.getRecords().stream().map(this::toStatusHistoryItem).toList())
            .setTotal(page.getTotal())
            .setPageNo(page.getCurrent())
            .setPageSize(page.getSize());
    }

    /**
     * Automatically restores expired governance statuses in small batches.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoRecoverExpiredStatuses(Date currentTime, Long batchSize) {
        long currentBatchSize = batchSize == null || batchSize < 1L ? 100L : batchSize;
        Date executionTime = currentTime == null ? new Date() : currentTime;
        IPage<ContentUserStatusRecord> page = statusRecordMapper.selectPage(
            new Page<>(1L, currentBatchSize),
            Wrappers.<ContentUserStatusRecord>lambdaQuery()
                .eq(ContentUserStatusRecord::getRecoverable, Boolean.TRUE)
                .isNotNull(ContentUserStatusRecord::getEffectiveEndTime)
                .le(ContentUserStatusRecord::getEffectiveEndTime, executionTime)
                .in(ContentUserStatusRecord::getTargetStatus, AUTO_RECOVERABLE_STATUSES)
                .orderByAsc(ContentUserStatusRecord::getEffectiveEndTime)
        );
        List<ContentUserStatusRecord> expiredRecords = page.getRecords();
        if (expiredRecords == null || expiredRecords.isEmpty() || profileMapper == null) {
            return 0;
        }
        Map<String, ContentUserProfile> profileMap = selectProfilesByUserIds(expiredRecords);
        int recoveredCount = 0;
        for (ContentUserStatusRecord expiredRecord : expiredRecords) {
            ContentUserProfile profile = profileMap.get(expiredRecord.getUserId());
            if (profile == null || !expiredRecord.getTargetStatus().equals(profile.getStatus())) {
                continue;
            }
            String restoredStatus = restoreProfileStatus(profile, expiredRecord, executionTime);
            if (restoredStatus == null) {
                continue;
            }
            auditLogMapper.insert(buildAutoRecoverAuditLog(expiredRecord, restoredStatus));
            growthPenaltyRecoveryService.recoverByGovernanceRecord(
                expiredRecord,
                AUTO_RECOVER_OPERATOR,
                executionTime,
                "处罚到期自动恢复"
            );
            recoveredCount++;
        }
        return recoveredCount;
    }

    /**
     * Lists recent device sessions for the target user.
     */
    @Override
    public List<ContentUserDeviceSession> listDeviceSessions(String userId) {
        if (deviceSessionMapper == null) {
            return List.of();
        }
        return deviceSessionMapper.selectList(
            Wrappers.<ContentUserDeviceSession>lambdaQuery()
                .eq(ContentUserDeviceSession::getUserId, userId)
                .orderByDesc(ContentUserDeviceSession::getLastActiveTime)
        );
    }

    /**
     * Marks the specified device session as offline.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineDeviceSession(String userId, String sessionId) {
        if (deviceSessionMapper == null) {
            throw new JeecgBootException("设备会话不存在");
        }
        ContentUserDeviceSession session = deviceSessionMapper.selectById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new JeecgBootException("设备会话不存在");
        }
        session.setOffline(Boolean.TRUE);
        session.setLastActiveTime(new Date());
        deviceSessionMapper.updateById(session);
    }

    /**
     * Deletes a comment as a moderator action.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(String operatorUserId, String commentId, String reason) {
        requireModeratorOrAdmin(operatorUserId);
        if (commentId == null || commentId.isBlank()) {
            throw new JeecgBootException("评论ID不能为空");
        }
        auditLogMapper.insert(ContentUserAuditLog.moderatorAction(
            null, operatorUserId, "DELETE_COMMENT", commentId, reason));
    }

    /**
     * Warns a user as a moderator action.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void warnUser(String operatorUserId, String targetUserId, String reason) {
        requireModeratorOrAdmin(operatorUserId);
        if (targetUserId == null || targetUserId.isBlank()) {
            throw new JeecgBootException("目标用户ID不能为空");
        }
        auditLogMapper.insert(ContentUserAuditLog.moderatorAction(
            targetUserId, operatorUserId, "WARN_USER", null, reason));
    }

    private void requireModeratorOrAdmin(String operatorUserId) {
        ContentUserProfile profile = profileMapper.selectByUserId(operatorUserId);
        String role = (profile != null && profile.getCommunityRole() != null) ? profile.getCommunityRole() : "NORMAL";
        if (!"MODERATOR".equals(role) && !"ADMIN".equals(role)) {
            throw new JeecgBootException("无权执行此操作，需要版主或管理员权限");
        }
    }

    private void validateTransition(ContentUserStatusChangeReq req) {
        if (!ContentUserStatusEnum.codes().contains(req.getCurrentStatus())) {
            throw new JeecgBootException("当前状态非法");
        }
        if (!ContentUserStatusEnum.codes().contains(req.getTargetStatus())) {
            throw new JeecgBootException("目标状态非法");
        }
        if (req.getCurrentStatus().equals(req.getTargetStatus())) {
            throw new JeecgBootException("状态未发生变化");
        }
        if (!isAllowedTransition(req.getCurrentStatus(), req.getTargetStatus())) {
            throw new JeecgBootException("状态流转不合法");
        }
    }

    private boolean isAllowedTransition(String currentStatus, String targetStatus) {
        if (ContentUserStatusEnum.CANCELLED.getCode().equals(currentStatus)) {
            return false;
        }
        if (ContentUserStatusEnum.CANCEL_PENDING.getCode().equals(currentStatus)) {
            return ContentUserStatusEnum.CANCELLED.getCode().equals(targetStatus)
                || ContentUserStatusEnum.NORMAL.getCode().equals(targetStatus);
        }
        if (ContentUserStatusEnum.GUEST.getCode().equals(currentStatus)) {
            return ContentUserStatusEnum.REGISTERED_INCOMPLETE.getCode().equals(targetStatus);
        }
        if (ContentUserStatusEnum.REGISTERED_INCOMPLETE.getCode().equals(currentStatus)) {
            return ContentUserStatusEnum.NORMAL.getCode().equals(targetStatus)
                || ContentUserStatusEnum.FROZEN.getCode().equals(targetStatus)
                || ContentUserStatusEnum.BANNED.getCode().equals(targetStatus);
        }
        if (ContentUserStatusEnum.NORMAL.getCode().equals(currentStatus)) {
            return ContentUserStatusEnum.MUTED.getCode().equals(targetStatus)
                || ContentUserStatusEnum.RECOMMENDATION_LIMITED.getCode().equals(targetStatus)
                || ContentUserStatusEnum.FROZEN.getCode().equals(targetStatus)
                || ContentUserStatusEnum.BANNED.getCode().equals(targetStatus)
                || ContentUserStatusEnum.CANCEL_PENDING.getCode().equals(targetStatus);
        }
        if (ContentUserStatusEnum.MUTED.getCode().equals(currentStatus)
            || ContentUserStatusEnum.RECOMMENDATION_LIMITED.getCode().equals(currentStatus)
            || ContentUserStatusEnum.FROZEN.getCode().equals(currentStatus)
            || ContentUserStatusEnum.BANNED.getCode().equals(currentStatus)) {
            return ContentUserStatusEnum.NORMAL.getCode().equals(targetStatus)
                || ContentUserStatusEnum.BANNED.getCode().equals(targetStatus)
                || ContentUserStatusEnum.FROZEN.getCode().equals(targetStatus)
                || ContentUserStatusEnum.CANCEL_PENDING.getCode().equals(targetStatus);
        }
        return false;
    }

    private void updateProfileStatus(String userId, String targetStatus) {
        if (profileMapper == null) {
            return;
        }
        ContentUserProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            return;
        }
        profile.setStatus(targetStatus);
        profileMapper.updateById(profile);
    }

    private String resolveCurrentStatus(String userId) {
        ContentUserProfile profile = profileMapper == null ? null : profileMapper.selectByUserId(userId);
        if (profile != null && profile.getStatus() != null) {
            return profile.getStatus();
        }
        ContentUserStatusRecord latestRecord = statusRecordMapper.selectLatestByUserId(userId);
        if (latestRecord != null && latestRecord.getTargetStatus() != null) {
            return latestRecord.getTargetStatus();
        }
        return ContentUserStatusEnum.GUEST.getCode();
    }

    private ContentUserStatusHistoryItemVO toStatusHistoryItem(ContentUserStatusRecord record) {
        return new ContentUserStatusHistoryItemVO()
            .setRecordId(record.getId())
            .setUserId(record.getUserId())
            .setCurrentStatus(record.getCurrentStatus())
            .setTargetStatus(record.getTargetStatus())
            .setTriggerSource(record.getTriggerSource())
            .setOperatorUserId(record.getOperatorUserId())
            .setReason(record.getReason())
            .setRuleCode(record.getRuleCode())
            .setEffectiveStartTime(record.getEffectiveStartTime())
            .setEffectiveEndTime(record.getEffectiveEndTime())
            .setRecoverable(record.getRecoverable())
            .setCreateTime(record.getCreateTime());
    }

    private Map<String, ContentUserProfile> selectProfilesByUserIds(List<ContentUserStatusRecord> expiredRecords) {
        Set<String> userIds = expiredRecords.stream()
            .map(ContentUserStatusRecord::getUserId)
            .filter(userId -> userId != null && !userId.isBlank())
            .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }
        return profileMapper.selectList(
            Wrappers.<ContentUserProfile>lambdaQuery().in(ContentUserProfile::getUserId, userIds)
        ).stream().collect(Collectors.toMap(ContentUserProfile::getUserId, profile -> profile, (left, right) -> left));
    }

    private String restoreProfileStatus(ContentUserProfile profile, ContentUserStatusRecord expiredRecord, Date executionTime) {
        String restoredStatus = expiredRecord.getCurrentStatus() == null || expiredRecord.getCurrentStatus().isBlank()
            ? ContentUserStatusEnum.NORMAL.getCode()
            : expiredRecord.getCurrentStatus();
        if (restoredStatus.equals(profile.getStatus())) {
            return null;
        }
        String currentStatus = profile.getStatus();
        profile.setStatus(restoredStatus);
        profileMapper.updateById(profile);
        ContentUserStatusRecord restoreRecord = new ContentUserStatusRecord();
        restoreRecord.setUserId(expiredRecord.getUserId());
        restoreRecord.setCurrentStatus(currentStatus);
        restoreRecord.setTargetStatus(restoredStatus);
        restoreRecord.setTriggerSource(AUTO_RECOVER_TRIGGER);
        restoreRecord.setOperatorUserId(AUTO_RECOVER_OPERATOR);
        restoreRecord.setReason("处罚到期自动恢复");
        restoreRecord.setEffectiveStartTime(executionTime);
        restoreRecord.setRecoverable(Boolean.FALSE);
        statusRecordMapper.insert(restoreRecord);
        return restoredStatus;
    }

    @Override
    public void deleteComment(String operatorUserId, String commentId, String reason) {
        ContentUserProfile operator = profileMapper.selectByUserId(operatorUserId);
        if (operator == null || !isModeratorOrAdmin(operator.getCommunityRole())) {
            throw new JeecgBootException("权限不足");
        }
        ContentUserAuditLog log = new ContentUserAuditLog()
            .setUserId(operatorUserId)
            .setEventType("COMMENT_DELETED")
            .setOperatorUserId(operatorUserId)
            .setEventContent(commentId)
            .setExtraDataJson("{\"reason\":\"" + reason + "\"}")
            .setEventTime(new Date());
        auditLogMapper.insert(log);
    }

    @Override
    public void warnUser(String operatorUserId, String targetUserId, String reason) {
        ContentUserProfile operator = profileMapper.selectByUserId(operatorUserId);
        if (operator == null || !isModeratorOrAdmin(operator.getCommunityRole())) {
            throw new JeecgBootException("权限不足");
        }
        ContentUserAuditLog log = new ContentUserAuditLog()
            .setUserId(targetUserId)
            .setEventType("USER_WARNED")
            .setOperatorUserId(operatorUserId)
            .setEventContent(reason)
            .setEventTime(new Date());
        auditLogMapper.insert(log);
    }

    private boolean isModeratorOrAdmin(String role) {
        ContentCommunityRoleEnum roleEnum = ContentCommunityRoleEnum.fromValue(role);
        return roleEnum == ContentCommunityRoleEnum.MODERATOR || roleEnum == ContentCommunityRoleEnum.ADMIN;
    }

    private ContentUserAuditLog buildAutoRecoverAuditLog(ContentUserStatusRecord expiredRecord, String restoredStatus) {
        ContentUserStatusChangeReq req = new ContentUserStatusChangeReq()
            .setUserId(expiredRecord.getUserId())
            .setCurrentStatus(expiredRecord.getTargetStatus())
            .setTargetStatus(restoredStatus)
            .setOperatorUserId(AUTO_RECOVER_OPERATOR)
            .setReason("处罚到期自动恢复");
        return ContentUserAuditLog.statusChange(req);
    }
}
