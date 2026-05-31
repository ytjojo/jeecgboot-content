package org.jeecg.modules.content.circle.biz;

import jakarta.annotation.Resource;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.jeecg.modules.content.circle.service.ICircleJoinReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 圈子加入申请审核业务编排服务。
 * 编排权限校验、审核操作、审计日志写入。
 */
@Service
public class CircleJoinReviewBizService {

    @Resource
    private ICircleJoinReviewService circleJoinReviewService;

    @Resource
    private ICircleAuditLogService circleAuditLogService;

    /**
     * 批准加入申请。
     *
     * @param requestId  申请ID
     * @param operatorId 操作人ID
     * @param circleId   圈子ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void approve(String requestId, String operatorId, String circleId) {
        // TODO: 调用 circle-core 角色服务校验
        circleJoinReviewService.approve(requestId, operatorId);
        writeAuditLog(circleId, operatorId, requestId, CircleAuditActionEnum.APPROVE_JOIN, null);
    }

    /**
     * 拒绝加入申请。
     *
     * @param requestId  申请ID
     * @param operatorId 操作人ID
     * @param circleId   圈子ID
     * @param reason     拒绝原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void reject(String requestId, String operatorId, String circleId, String reason) {
        // TODO: 调用 circle-core 角色服务校验
        circleJoinReviewService.reject(requestId, operatorId, reason);
        writeAuditLog(circleId, operatorId, requestId, CircleAuditActionEnum.REJECT_JOIN, reason);
    }

    private void writeAuditLog(String circleId, String operatorId, String targetId,
                               CircleAuditActionEnum action, String reason) {
        CircleAuditLog log = new CircleAuditLog();
        log.setCircleId(circleId);
        log.setOperatorId(operatorId);
        log.setAction(action.getCode());
        log.setTargetId(targetId);
        log.setTargetType("JOIN_REQUEST");
        log.setResult("SUCCESS");
        log.setReason(reason);
        circleAuditLogService.writeAuditLog(log);
    }
}
