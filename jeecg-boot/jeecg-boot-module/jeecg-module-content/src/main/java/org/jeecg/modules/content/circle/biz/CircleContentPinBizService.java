package org.jeecg.modules.content.circle.biz;

import jakarta.annotation.Resource;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.jeecg.modules.content.circle.service.ICircleContentPinService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 圈子内容置顶/精华业务编排服务。
 * 编排权限校验、操作执行、审计日志写入。
 */
@Service
public class CircleContentPinBizService {

    @Resource
    private ICircleContentPinService circleContentPinService;

    @Resource
    private ICircleAuditLogService circleAuditLogService;

    /**
     * 置顶内容
     *
     * @param contentId  内容ID
     * @param operatorId 操作人ID
     * @param circleId   圈子ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void pin(String contentId, String operatorId, String circleId) {
        // TODO: 调用 circle-core 角色服务校验
        circleContentPinService.pinContent(contentId);
        writeAuditLog(circleId, operatorId, contentId, CircleAuditActionEnum.PIN);
    }

    /**
     * 取消置顶
     *
     * @param contentId  内容ID
     * @param operatorId 操作人ID
     * @param circleId   圈子ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void unpin(String contentId, String operatorId, String circleId) {
        // TODO: 调用 circle-core 角色服务校验
        circleContentPinService.unpinContent(contentId);
        writeAuditLog(circleId, operatorId, contentId, CircleAuditActionEnum.UNPIN);
    }

    /**
     * 设为精华
     *
     * @param contentId  内容ID
     * @param operatorId 操作人ID
     * @param circleId   圈子ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void feature(String contentId, String operatorId, String circleId) {
        // TODO: 调用 circle-core 角色服务校验
        circleContentPinService.featureContent(contentId);
        writeAuditLog(circleId, operatorId, contentId, CircleAuditActionEnum.FEATURE);
    }

    /**
     * 取消精华
     *
     * @param contentId  内容ID
     * @param operatorId 操作人ID
     * @param circleId   圈子ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfeature(String contentId, String operatorId, String circleId) {
        // TODO: 调用 circle-core 角色服务校验
        circleContentPinService.unfeatureContent(contentId);
        writeAuditLog(circleId, operatorId, contentId, CircleAuditActionEnum.UNFEATURE);
    }

    private void writeAuditLog(String circleId, String operatorId, String contentId,
                               CircleAuditActionEnum action) {
        CircleAuditLog log = new CircleAuditLog();
        log.setCircleId(circleId);
        log.setOperatorId(operatorId);
        log.setAction(action.getCode());
        log.setTargetId(contentId);
        log.setTargetType("CONTENT");
        log.setResult("SUCCESS");
        circleAuditLogService.writeAuditLog(log);
    }
}
