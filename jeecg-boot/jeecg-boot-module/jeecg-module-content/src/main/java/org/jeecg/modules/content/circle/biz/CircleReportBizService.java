package org.jeecg.modules.content.circle.biz;

import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.entity.CircleReport;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
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

    @Resource
    private ICircleMemberService circleMemberService;

    /**
     * 提交举报。
     *
     * @param report     举报对象
     * @param reporterId 举报人ID
     */
    public void submitReport(CircleReport report, String reporterId) {
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
        checkManagePermission(circleId, operatorId);
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
        checkManagePermission(circleId, operatorId);
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
        checkManagePermission(circleId, operatorId);
        circleReportService.handleMute(reportId, operatorId);
        writeAuditLog(circleId, operatorId, reportId, CircleAuditActionEnum.MUTE_FROM_REPORT);
    }

    /**
     * 校验操作人是否有管理权限（创建者或版主）。
     *
     * @param circleId   圈子ID
     * @param operatorId 操作人ID
     * @throws JeecgBootException 权限不足时抛出
     */
    private void checkManagePermission(String circleId, String operatorId) {
        CircleMember operator = circleMemberService.findByCircleAndUser(circleId, operatorId);
        if (operator == null || operator.getRole() == CircleMember.Role.MEMBER) {
            throw new JeecgBootException("权限不足，仅创建者和版主可管理举报");
        }
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
