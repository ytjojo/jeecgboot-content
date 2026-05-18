package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.req.support.ContentAppealHandleReq;
import org.jeecg.modules.content.user.req.support.ContentReportCreateReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;

import java.util.Date;

/**
 * Entity for content user audit log.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_audit_log")
@Schema(description = "内容社区用户审计日志")
public class ContentUserAuditLog extends JeecgEntity {

    @Schema(description = "关联用户ID")
    private String userId;

    @Schema(description = "事件类型")
    private String eventType;

    @Schema(description = "操作人")
    private String operatorUserId;

    @Schema(description = "事件内容")
    private String eventContent;

    @Schema(description = "追踪ID")
    private String traceId;

    @Schema(description = "额外数据JSON")
    private String extraDataJson;

    @Schema(description = "事件时间")
    private Date eventTime;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "设备信息")
    private String deviceInfo;

    /**
     * Executes the statusChange operation.
     */
    public static ContentUserAuditLog statusChange(ContentUserStatusChangeReq req) {
        return new ContentUserAuditLog()
            .setUserId(req.getUserId())
            .setEventType("USER_STATUS_CHANGE")
            .setOperatorUserId(req.getOperatorUserId())
            .setEventContent(req.getCurrentStatus() + " -> " + req.getTargetStatus())
            .setExtraDataJson(req.getReason())
            .setEventTime(new Date());
    }

    /**
     * Executes the behaviorAwarded operation.
     */
    public static ContentUserAuditLog behaviorAwarded(String userId, String sourceType, int pointDelta, int growthDelta) {
        return new ContentUserAuditLog()
            .setUserId(userId)
            .setEventType("USER_GROWTH_RECORDED")
            .setEventContent(sourceType)
            .setExtraDataJson("{\"pointDelta\":" + pointDelta + ",\"growthDelta\":" + growthDelta + "}")
            .setEventTime(new Date());
    }

    /**
     * 记录勋章违规回收审计事件，并作为用户通知流水来源。
     */
    public static ContentUserAuditLog badgeRecycled(String userId, String operatorUserId, String badgeCode, String reason) {
        return new ContentUserAuditLog()
            .setUserId(userId)
            .setOperatorUserId(operatorUserId)
            .setEventType("USER_BADGE_RECYCLED")
            .setEventContent(badgeCode)
            .setExtraDataJson("{\"reason\":\"" + reason + "\",\"notification\":true}")
            .setEventTime(new Date());
    }

    /**
     * Executes the appealCreated operation.
     */
    public static ContentUserAuditLog appealCreated(ContentUserAppeal appeal) {
        return new ContentUserAuditLog()
            .setUserId(appeal.getUserId())
            .setEventType("USER_APPEAL_CREATED")
            .setEventContent(appeal.getAppealType() + ":" + appeal.getTargetType())
            .setExtraDataJson(appeal.getTargetId())
            .setEventTime(new Date());
    }

    /**
     * Executes the reportCreated operation.
     */
    public static ContentUserAuditLog reportCreated(String reportId, ContentReportCreateReq req) {
        return new ContentUserAuditLog()
            .setUserId(req.getUserId())
            .setEventType("USER_REPORT_CREATED")
            .setEventContent(req.getReportType() + ":" + req.getTargetType())
            .setExtraDataJson("{\"reportId\":\"" + reportId + "\",\"targetId\":\"" + req.getTargetId()
                + "\",\"reason\":\"" + req.getReason() + "\"}")
            .setEventTime(new Date());
    }

    /**
     * Executes the appealHandled operation.
     */
    public static ContentUserAuditLog appealHandled(ContentUserAppeal appeal, ContentAppealHandleReq req) {
        return new ContentUserAuditLog()
            .setUserId(appeal.getUserId())
            .setOperatorUserId(req.getOperatorUserId())
            .setEventType("USER_APPEAL_HANDLED")
            .setEventContent(appeal.getAppealType() + ":" + req.getResultStatus())
            .setExtraDataJson("{\"appealId\":\"" + appeal.getId() + "\",\"resultStatus\":\""
                + req.getResultStatus() + "\",\"resultNote\":\"" + req.getResultNote() + "\"}")
            .setEventTime(new Date());
    }

    /**
     * Executes the reportHandled operation.
     */
    public static ContentUserAuditLog reportHandled(ContentUserReport report, ContentReportHandleReq req) {
        return new ContentUserAuditLog()
            .setUserId(report.getUserId())
            .setOperatorUserId(req.getOperatorUserId())
            .setEventType("USER_REPORT_HANDLED")
            .setEventContent(report.getReportType() + ":" + req.getResultStatus())
            .setExtraDataJson("{\"reportId\":\"" + report.getId() + "\",\"resultStatus\":\""
                + req.getResultStatus() + "\",\"resultNote\":\"" + req.getResultNote() + "\"}")
            .setEventTime(new Date());
    }

    /**
     * Executes the growthPenaltyRecovered operation.
     */
    public static ContentUserAuditLog growthPenaltyRecovered(String userId,
                                                             String operatorUserId,
                                                             String trigger,
                                                             String penaltyRecordId,
                                                             int pointDelta,
                                                             int growthDelta,
                                                             int badgeCount,
                                                             int recoveredBenefitCount) {
        return new ContentUserAuditLog()
            .setUserId(userId)
            .setOperatorUserId(operatorUserId)
            .setEventType("USER_GROWTH_PENALTY_RECOVERED")
            .setEventContent(trigger)
            .setExtraDataJson("{\"penaltyRecordId\":\"" + penaltyRecordId
                + "\",\"pointDelta\":" + pointDelta
                + ",\"growthDelta\":" + growthDelta
                + ",\"badgeCount\":" + badgeCount
                + ",\"recoveredBenefitCount\":" + recoveredBenefitCount + "}")
            .setEventTime(new Date());
    }

    /**
     * Executes the growthPenaltyExecuted operation.
     */
    public static ContentUserAuditLog growthPenaltyExecuted(String userId,
                                                            String operatorUserId,
                                                            String sourceType,
                                                            String penaltyRecordId,
                                                            int pointDelta,
                                                            int growthDelta,
                                                            int badgeCount,
                                                            int benefitCount) {
        return new ContentUserAuditLog()
            .setUserId(userId)
            .setOperatorUserId(operatorUserId)
            .setEventType("USER_GROWTH_PENALTY_EXECUTED")
            .setEventContent(sourceType)
            .setExtraDataJson("{\"penaltyRecordId\":\"" + penaltyRecordId
                + "\",\"pointDelta\":" + pointDelta
                + ",\"growthDelta\":" + growthDelta
                + ",\"badgeCount\":" + badgeCount
                + ",\"benefitCount\":" + benefitCount + "}")
            .setEventTime(new Date());
    }

    /**
     * 记录手机号绑定审计事件。
     */
    public static ContentUserAuditLog accountMobileBound(String userId, String operatorUserId, String maskedMobile) {
        return new ContentUserAuditLog()
            .setUserId(userId)
            .setOperatorUserId(operatorUserId)
            .setEventType("USER_ACCOUNT_MOBILE_BOUND")
            .setEventContent("bind_mobile")
            .setExtraDataJson("{\"mobile\":\"" + maskedMobile + "\"}")
            .setEventTime(new Date());
    }

    /**
     * 记录邮箱绑定审计事件。
     */
    public static ContentUserAuditLog accountEmailBound(String userId, String operatorUserId, String maskedEmail) {
        return new ContentUserAuditLog()
            .setUserId(userId)
            .setOperatorUserId(operatorUserId)
            .setEventType("USER_ACCOUNT_EMAIL_BOUND")
            .setEventContent("bind_email")
            .setExtraDataJson("{\"email\":\"" + maskedEmail + "\"}")
            .setEventTime(new Date());
    }

    /**
     * 记录手机号解绑审计事件。
     */
    public static ContentUserAuditLog accountMobileUnbound(String userId, String operatorUserId) {
        return new ContentUserAuditLog()
            .setUserId(userId)
            .setOperatorUserId(operatorUserId)
            .setEventType("USER_ACCOUNT_MOBILE_UNBOUND")
            .setEventContent("unbind_mobile")
            .setEventTime(new Date());
    }

    /**
     * 记录邮箱解绑审计事件。
     */
    public static ContentUserAuditLog accountEmailUnbound(String userId, String operatorUserId) {
        return new ContentUserAuditLog()
            .setUserId(userId)
            .setOperatorUserId(operatorUserId)
            .setEventType("USER_ACCOUNT_EMAIL_UNBOUND")
            .setEventContent("unbind_email")
            .setEventTime(new Date());
    }
}
