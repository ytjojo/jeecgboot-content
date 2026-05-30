package org.jeecg.modules.content.circle.biz;

import jakarta.annotation.Resource;
import org.jeecg.modules.content.circle.entity.CircleAnnouncement;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.service.ICircleAnnouncementService;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 圈子公告业务编排服务。
 * 编排权限校验、公告发布、审计日志写入。
 */
@Service
public class CircleAnnouncementBizService {

    @Resource
    private ICircleAnnouncementService circleAnnouncementService;

    @Resource
    private ICircleAuditLogService circleAuditLogService;

    /**
     * 发布公告（含旧公告替换）
     *
     * @param announcement 公告对象
     * @param operatorId   操作人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void publish(CircleAnnouncement announcement, String operatorId) {
        // TODO: 调用 circle-core 角色服务校验
        circleAnnouncementService.publish(announcement);
        writeAuditLog(announcement.getCircleId(), operatorId, announcement.getId(),
                CircleAuditActionEnum.PUBLISH_ANNOUNCEMENT);
    }

    private void writeAuditLog(String circleId, String operatorId, String targetId,
                               CircleAuditActionEnum action) {
        CircleAuditLog log = new CircleAuditLog();
        log.setCircleId(circleId);
        log.setOperatorId(operatorId);
        log.setAction(action.getCode());
        log.setTargetId(targetId);
        log.setTargetType("ANNOUNCEMENT");
        log.setResult("SUCCESS");
        circleAuditLogService.writeAuditLog(log);
    }
}
