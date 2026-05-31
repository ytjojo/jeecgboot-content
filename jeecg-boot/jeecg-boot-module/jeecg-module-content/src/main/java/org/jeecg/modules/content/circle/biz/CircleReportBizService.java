package org.jeecg.modules.content.circle.biz;

import jakarta.annotation.Resource;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.entity.CircleReport;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.jeecg.modules.content.circle.service.ICircleReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 圈子内容举报业务编排服务。
 * 编排权限校验、举报处理、审计日志写入。
 */
@Service
public class CircleReportBizService {

    @Resource
    private ICircleReportService circleReportService;

    @Resource
    private ICircleAuditLogService circleAuditLogService;

    /**
     * 提交举报。
     *
     * @param report     举报对象
     * @param reporterId 举报人ID
     */
    public void submitReport(CircleReport report, String reporterId) {
        // TODO: 权限校验
        report.setReporterId(reporterId);
        circleReportService.submitReport(report);
    }

    /**
     * 处理举报：删除被举报内容。
     *
     * @param reportId   举报ID
     * @param operatorId 处理人ID
     * @param circleId   圈子ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleDeleteContent(String reportId, String operatorId, String circleId) {
        // TODO: 权限校验
        circleReportService.handleDeleteContent(reportId, operatorId);
        writeAuditLog(circleId, operatorId, reportId, CircleAuditActionEnum.DELETE_REPORTED);
    }

    /**
     * 处理举报：忽略。
     *
     * @param reportId   举报ID
     * @param operatorId 处理人ID
     * @param circleId   圈子ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleIgnore(String reportId, String operatorId, String circleId) {
        // TODO: 权限校验
        circleReportService.handleIgnore(reportId, operatorId);
        writeAuditLog(circleId, operatorId, reportId, CircleAuditActionEnum.IGNORE_REPORT);
    }

    /**
     * 处理举报：禁言被举报用户。
     *
     * @param reportId   举报ID
     * @param operatorId 处理人ID
     * @param circleId   圈子ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleMute(String reportId, String operatorId, String circleId) {
        // TODO: 权限校验
        circleReportService.handleMute(reportId, operatorId);
        writeAuditLog(circleId, operatorId, reportId, CircleAuditActionEnum.MUTE_FROM_REPORT);
    }

    private void writeAuditLog(String circleId, String operatorId, String targetId,
                               CircleAuditActionEnum action) {
        CircleAuditLog log = new CircleAuditLog();
        log.setCircleId(circleId);
        log.setOperatorId(operatorId);
        log.setAction(action.getCode());
        log.setTargetId(targetId);
        log.setTargetType("REPORT");
        log.setResult("SUCCESS");
        circleAuditLogService.writeAuditLog(log);
    }
}
