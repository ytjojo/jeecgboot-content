package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.enums.ContentUserStatusEnum;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserStatusRecordMapper;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.service.IContentUserGovernanceService;
import org.jeecg.modules.content.user.vo.ContentUserStatusVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Service implementation for content user governance.
 */
@Service
public class ContentUserGovernanceServiceImpl implements IContentUserGovernanceService {

    @Resource
    private ContentUserStatusRecordMapper statusRecordMapper;

    @Resource
    private ContentUserAuditLogMapper auditLogMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserDeviceSessionMapper deviceSessionMapper;

    /**
     * Changes the lifecycle status of the target user and records governance logs.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(ContentUserStatusChangeReq req) {
        validateTransition(req);
        ContentUserStatusRecord record = ContentUserStatusRecord.from(req);
        statusRecordMapper.insert(record);
        auditLogMapper.insert(ContentUserAuditLog.statusChange(req));
        updateProfileStatus(req.getUserId(), req.getTargetStatus());
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
}
