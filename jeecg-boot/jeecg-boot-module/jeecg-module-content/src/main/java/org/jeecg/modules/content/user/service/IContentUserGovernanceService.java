package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.vo.ContentUserAuditLogPageVO;
import org.jeecg.modules.content.user.vo.ContentUserStatusHistoryPageVO;
import org.jeecg.modules.content.user.vo.ContentUserStatusVO;

import java.util.Date;
import java.util.List;

/**
 * Service contract for content user governance.
 */
public interface IContentUserGovernanceService {

    void changeStatus(ContentUserStatusChangeReq req);

    boolean canExecuteAction(String userId, String actionType);

    ContentUserStatusVO getCurrentStatus(String userId);

    ContentUserStatusHistoryPageVO listStatusHistory(String userId, Long pageNo, Long pageSize);

    int autoRecoverExpiredStatuses(Date currentTime, Long batchSize);

    List<ContentUserDeviceSession> listDeviceSessions(String userId);

    void offlineDeviceSession(String userId, String sessionId);

    /**
     * Deletes a comment as a moderator action.
     *
     * @param operatorUserId the user performing the action (must be MODERATOR or ADMIN)
     * @param commentId      the comment to delete
     * @param reason         the reason for deletion
     */
    void deleteComment(String operatorUserId, String commentId, String reason);

    /**
     * Warns a user as a moderator action.
     *
     * @param operatorUserId the user performing the action (must be MODERATOR or ADMIN)
     * @param targetUserId   the user to warn
     * @param reason         the reason for the warning
     */
    void warnUser(String operatorUserId, String targetUserId, String reason);

    /**
     * 分页查询审计日志。
     *
     * @param pageNo         页码
     * @param pageSize       每页条数
     * @param operatorUserId 操作人ID（可选）
     * @param eventType      事件类型（可选）
     * @param startTime      开始时间（可选）
     * @param endTime        结束时间（可选）
     * @return 分页结果
     */
    ContentUserAuditLogPageVO listAuditLog(Long pageNo, Long pageSize,
                                           String operatorUserId, String eventType,
                                           Date startTime, Date endTime);
}
