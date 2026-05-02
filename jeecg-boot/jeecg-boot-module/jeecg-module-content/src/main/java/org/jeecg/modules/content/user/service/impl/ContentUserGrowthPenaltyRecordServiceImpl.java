package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserBadgeGrant;
import org.jeecg.modules.content.user.entity.ContentUserGrowthLedger;
import org.jeecg.modules.content.user.entity.ContentUserGrowthPenaltyRecord;
import org.jeecg.modules.content.user.entity.ContentUserLevelBenefitPenaltyRecord;
import org.jeecg.modules.content.user.entity.ContentUserPointLedger;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserReport;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserBadgeGrantMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthPenaltyRecordMapper;
import org.jeecg.modules.content.user.mapper.ContentUserLevelBenefitPenaltyRecordMapper;
import org.jeecg.modules.content.user.mapper.ContentUserPointLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;
import org.jeecg.modules.content.user.service.IContentUserGrowthPenaltyRecordService;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Service implementation for growth penalty record creation.
 */
@Service
public class ContentUserGrowthPenaltyRecordServiceImpl implements IContentUserGrowthPenaltyRecordService {

    private static final String STATUS_PENDING_RECOVER = "PENDING_RECOVER";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String SOURCE_GOVERNANCE = "GOVERNANCE_STATUS_CHANGE";
    private static final String SOURCE_REPORT = "REPORT_HANDLE";
    private static final String SOURCE_TYPE_GROWTH_PENALTY = "GROWTH_PENALTY";
    private static final String PENALTY_COMPOSITE = "COMPOSITE";
    private static final String BENEFIT_PRIORITY_CUSTOMER_SERVICE = "PRIORITY_CUSTOMER_SERVICE";
    private static final String BENEFIT_RECOVER_STATUS_PENDING = "PENDING_RECOVER";
    private static final String BADGE_STATUS_ACTIVE = "ACTIVE";
    private static final String BADGE_STATUS_RECYCLED = "RECYCLED";
    private static final int POINT_PENALTY_DELTA = 20;
    private static final int GROWTH_PENALTY_DELTA = 30;
    private static final Set<String> PUNISHING_STATUSES = Set.of(
        "MUTED",
        "RECOMMENDATION_LIMITED",
        "FROZEN",
        "BANNED"
    );
    private static final Set<String> PUNISHING_REPORT_RESULTS = Set.of("CONFIRMED");

    @Resource
    private ContentUserGrowthPenaltyRecordMapper growthPenaltyRecordMapper;

    @Resource
    private ContentUserPointLedgerMapper pointLedgerMapper;

    @Resource
    private ContentUserGrowthLedgerMapper growthLedgerMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserBadgeGrantMapper badgeGrantMapper;

    @Resource
    private ContentUserLevelBenefitPenaltyRecordMapper levelBenefitPenaltyRecordMapper;

    @Resource
    private ContentUserAuditLogMapper auditLogMapper;

    @Resource
    private IContentUserLevelBenefitService levelBenefitService;

    @Override
    public void createFromGovernanceRecord(ContentUserStatusRecord record,
                                           ContentUserStatusChangeReq req,
                                           Date executeTime) {
        if (record == null || !StringUtils.hasText(record.getId()) || !isPunishingStatus(record.getTargetStatus())) {
            return;
        }
        ContentUserGrowthPenaltyRecord existing = growthPenaltyRecordMapper.selectOne(
            Wrappers.<ContentUserGrowthPenaltyRecord>lambdaQuery()
                .eq(ContentUserGrowthPenaltyRecord::getGovernanceRecordId, record.getId())
                .ne(ContentUserGrowthPenaltyRecord::getStatus, STATUS_CANCELLED)
                .last("limit 1")
        );
        if (existing != null) {
            return;
        }
        ContentUserProfile profile = profileMapper.selectByUserId(record.getUserId());
        if (profile == null) {
            return;
        }
        PenaltyExecutionResult result = executePenalty(profile, record.getId(), req == null ? null : req.getOperatorUserId(),
            req == null ? null : req.getReason(), req == null ? null : req.getRuleCode(), record.getTargetStatus());
        ContentUserGrowthPenaltyRecord item = new ContentUserGrowthPenaltyRecord();
        item.setId(UUIDGenerator.generate());
        item.setUserId(record.getUserId());
        item.setGovernanceRecordId(record.getId());
        item.setSourceType(SOURCE_GOVERNANCE);
        item.setSourceId(record.getId());
        item.setSourceStatus(record.getTargetStatus());
        item.setPenaltyType(PENALTY_COMPOSITE);
        item.setEffectSnapshotJson(buildSnapshotJson(result));
        item.setStatus(STATUS_PENDING_RECOVER);
        item.setCreateTime(resolveExecuteTime(executeTime));
        growthPenaltyRecordMapper.insert(item);
        persistBenefitPenaltyRecords(item, result, resolveExecuteTime(executeTime));
        writeExecutionAudit(item, result);
    }

    @Override
    public void createFromReportHandle(ContentUserReport report,
                                       ContentReportHandleReq req,
                                       String governanceRecordId,
                                       Date executeTime) {
        if (report == null || !StringUtils.hasText(report.getId())
            || !PUNISHING_REPORT_RESULTS.contains(report.getResultStatus())) {
            return;
        }
        ContentUserGrowthPenaltyRecord existing = growthPenaltyRecordMapper.selectOne(
            Wrappers.<ContentUserGrowthPenaltyRecord>lambdaQuery()
                .eq(ContentUserGrowthPenaltyRecord::getSourceType, SOURCE_REPORT)
                .eq(ContentUserGrowthPenaltyRecord::getSourceId, report.getId())
                .ne(ContentUserGrowthPenaltyRecord::getStatus, STATUS_CANCELLED)
                .last("limit 1")
        );
        if (existing != null) {
            return;
        }
        ContentUserProfile profile = profileMapper.selectByUserId(report.getUserId());
        if (profile == null) {
            return;
        }
        PenaltyExecutionResult result = executePenalty(profile, report.getId(), req == null ? null : req.getOperatorUserId(),
            req == null ? null : req.getResultNote(), null, report.getResultStatus());
        ContentUserGrowthPenaltyRecord item = new ContentUserGrowthPenaltyRecord();
        item.setId(UUIDGenerator.generate());
        item.setUserId(report.getUserId());
        item.setGovernanceRecordId(governanceRecordId);
        item.setSourceType(SOURCE_REPORT);
        item.setSourceId(report.getId());
        item.setSourceStatus(report.getResultStatus());
        item.setPenaltyType(PENALTY_COMPOSITE);
        item.setEffectSnapshotJson(buildSnapshotJson(result));
        item.setStatus(STATUS_PENDING_RECOVER);
        item.setCreateTime(resolveExecuteTime(executeTime));
        growthPenaltyRecordMapper.insert(item);
        persistBenefitPenaltyRecords(item, result, resolveExecuteTime(executeTime));
        writeExecutionAudit(item, result);
    }

    private boolean isPunishingStatus(String targetStatus) {
        return PUNISHING_STATUSES.contains(targetStatus);
    }

    private Date resolveExecuteTime(Date executeTime) {
        return executeTime == null ? new Date() : executeTime;
    }

    private PenaltyExecutionResult executePenalty(ContentUserProfile profile,
                                                  String bizId,
                                                  String operatorUserId,
                                                  String reason,
                                                  String ruleCode,
                                                  String sourceStatus) {
        PenaltyExecutionResult result = new PenaltyExecutionResult();
        result.operatorUserId = operatorUserId;
        result.reason = reason;
        result.ruleCode = ruleCode;
        result.sourceStatus = sourceStatus;
        applyPointPenalty(profile, bizId, sourceStatus, result);
        applyGrowthPenalty(profile, bizId, sourceStatus, result);
        applyBadgePenalty(profile.getUserId(), result);
        applyLevelBenefitPenalty(result);
        profileMapper.updateById(profile);
        return result;
    }

    private void applyPointPenalty(ContentUserProfile profile,
                                   String bizId,
                                   String sourceStatus,
                                   PenaltyExecutionResult result) {
        int before = defaultZero(profile.getPointBalance());
        int actualDelta = Math.min(before, POINT_PENALTY_DELTA);
        if (actualDelta <= 0) {
            return;
        }
        int after = before - actualDelta;
        profile.setPointBalance(after);
        pointLedgerMapper.insert(ContentUserPointLedger.of(profile.getUserId(), SOURCE_TYPE_GROWTH_PENALTY, bizId,
                -actualDelta, sourceStatus)
            .setBalanceAfter(after));
        result.pointDelta = -actualDelta;
        result.pointBefore = before;
        result.pointAfter = after;
    }

    private void applyGrowthPenalty(ContentUserProfile profile,
                                    String bizId,
                                    String sourceStatus,
                                    PenaltyExecutionResult result) {
        int beforeGrowth = defaultZero(profile.getGrowthValue());
        int beforeLevel = defaultLevel(profile.getLevel(), profile.getGrowthValue());
        int actualDelta = Math.min(beforeGrowth, GROWTH_PENALTY_DELTA);
        if (actualDelta <= 0) {
            return;
        }
        int afterGrowth = beforeGrowth - actualDelta;
        profile.setGrowthValue(afterGrowth);
        profile.setLevel(calculateLevel(afterGrowth));
        growthLedgerMapper.insert(ContentUserGrowthLedger.of(profile.getUserId(), SOURCE_TYPE_GROWTH_PENALTY, bizId,
                -actualDelta, sourceStatus)
            .setGrowthAfter(afterGrowth));
        result.growthDelta = -actualDelta;
        result.growthBefore = beforeGrowth;
        result.growthAfter = afterGrowth;
        result.levelBefore = beforeLevel;
        result.levelAfter = profile.getLevel();
    }

    private void applyBadgePenalty(String userId, PenaltyExecutionResult result) {
        List<ContentUserBadgeGrant> badgeGrants = badgeGrantMapper.selectList(
            Wrappers.<ContentUserBadgeGrant>lambdaQuery().eq(ContentUserBadgeGrant::getUserId, userId)
        );
        if (badgeGrants == null || badgeGrants.isEmpty()) {
            return;
        }
        for (ContentUserBadgeGrant item : badgeGrants) {
            if (!BADGE_STATUS_ACTIVE.equals(item.getStatus()) || !Boolean.TRUE.equals(item.getDisplaying())) {
                continue;
            }
            result.badgeEffectsJson.append(result.badgeEffectCount > 0 ? "," : "")
                .append("{\"badgeGrantId\":\"").append(escapeJson(item.getId()))
                .append("\",\"badgeCode\":\"").append(escapeJson(item.getBadgeCode()))
                .append("\",\"previousStatus\":\"").append(escapeJson(item.getStatus()))
                .append("\",\"previousDisplaying\":").append(Boolean.TRUE.equals(item.getDisplaying()))
                .append(",\"currentStatus\":\"").append(BADGE_STATUS_RECYCLED)
                .append("\",\"currentDisplaying\":false}");
            result.badgeEffectCount++;
            item.setStatus(BADGE_STATUS_RECYCLED);
            item.setDisplaying(Boolean.FALSE);
            item.setRecycledAt(new Date());
            badgeGrantMapper.updateById(item);
        }
    }

    private void applyLevelBenefitPenalty(PenaltyExecutionResult result) {
        result.benefitEffectsJson.append("{\"benefitCode\":\"").append(BENEFIT_PRIORITY_CUSTOMER_SERVICE)
            .append("\",\"previousEnabled\":true,\"currentEnabled\":false}");
        result.benefitEffectCount = 1;
    }

    private void persistBenefitPenaltyRecords(ContentUserGrowthPenaltyRecord item,
                                              PenaltyExecutionResult result,
                                              Date executeTime) {
        if (result.benefitEffectCount <= 0) {
            return;
        }
        ContentUserLevelBenefitPenaltyRecord benefitRecord = new ContentUserLevelBenefitPenaltyRecord();
        benefitRecord.setId(UUIDGenerator.generate());
        benefitRecord.setPenaltyRecordId(item.getId());
        benefitRecord.setUserId(item.getUserId());
        benefitRecord.setBenefitCode(BENEFIT_PRIORITY_CUSTOMER_SERVICE);
        boolean previousEnabled = levelBenefitService != null
            && levelBenefitService.hasEnabledBenefit(item.getUserId(), BENEFIT_PRIORITY_CUSTOMER_SERVICE);
        benefitRecord.setPreviousEnabled(previousEnabled);
        benefitRecord.setCurrentEnabled(Boolean.FALSE);
        benefitRecord.setRecoverStatus(BENEFIT_RECOVER_STATUS_PENDING);
        benefitRecord.setCreateTime(executeTime);
        levelBenefitPenaltyRecordMapper.insert(benefitRecord);
    }

    private void writeExecutionAudit(ContentUserGrowthPenaltyRecord item, PenaltyExecutionResult result) {
        if (auditLogMapper == null) {
            return;
        }
        auditLogMapper.insert(ContentUserAuditLog.growthPenaltyExecuted(
            item.getUserId(),
            result.operatorUserId,
            item.getSourceType(),
            item.getId(),
            result.pointDelta,
            result.growthDelta,
            result.badgeEffectCount,
            result.benefitEffectCount
        ));
    }

    private String buildSnapshotJson(PenaltyExecutionResult result) {
        StringBuilder plannedEffects = new StringBuilder("[");
        boolean hasPreviousEffect = false;
        if (result.pointDelta != 0) {
            plannedEffects.append("\"POINT_DEDUCT\"");
            hasPreviousEffect = true;
        }
        if (result.growthDelta != 0) {
            plannedEffects.append(hasPreviousEffect ? ",\"GROWTH_DEDUCT\"" : "\"GROWTH_DEDUCT\"");
            hasPreviousEffect = true;
        }
        if (result.badgeEffectCount > 0) {
            plannedEffects.append(hasPreviousEffect ? ",\"BADGE_DISABLE\"" : "\"BADGE_DISABLE\"");
            hasPreviousEffect = true;
        }
        if (result.benefitEffectCount > 0) {
            plannedEffects.append(hasPreviousEffect ? ",\"LEVEL_BENEFIT_DISABLE\"" : "\"LEVEL_BENEFIT_DISABLE\"");
        }
        plannedEffects.append("]");
        return "{\"operatorUserId\":\"" + escapeJson(result.operatorUserId)
            + "\",\"reason\":\"" + escapeJson(result.reason)
            + "\",\"ruleCode\":\"" + escapeJson(result.ruleCode)
            + "\",\"sourceStatus\":\"" + escapeJson(result.sourceStatus)
            + "\",\"plannedEffects\":" + plannedEffects
            + ",\"pointEffect\":{\"delta\":" + result.pointDelta
            + ",\"balanceBefore\":" + result.pointBefore
            + ",\"balanceAfter\":" + result.pointAfter + "}"
            + ",\"growthEffect\":{\"delta\":" + result.growthDelta
            + ",\"growthBefore\":" + result.growthBefore
            + ",\"growthAfter\":" + result.growthAfter
            + ",\"levelBefore\":" + result.levelBefore
            + ",\"levelAfter\":" + result.levelAfter + "}"
            + ",\"badgeEffects\":[" + result.badgeEffectsJson + "]"
            + ",\"benefitEffects\":[" + result.benefitEffectsJson + "]}";
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private int defaultLevel(Integer level, Integer growthValue) {
        return level == null ? calculateLevel(defaultZero(growthValue)) : level;
    }

    private int calculateLevel(Integer growthValue) {
        int safeGrowth = Math.max(defaultZero(growthValue), 0);
        return Math.max(1, safeGrowth / 100 + 1);
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final class PenaltyExecutionResult {

        private String operatorUserId;
        private String reason;
        private String ruleCode;
        private String sourceStatus;
        private int pointDelta;
        private int pointBefore;
        private int pointAfter;
        private int growthDelta;
        private int growthBefore;
        private int growthAfter;
        private int levelBefore;
        private int levelAfter;
        private int badgeEffectCount;
        private int benefitEffectCount;
        private final StringBuilder badgeEffectsJson = new StringBuilder();
        private final StringBuilder benefitEffectsJson = new StringBuilder();
    }
}
