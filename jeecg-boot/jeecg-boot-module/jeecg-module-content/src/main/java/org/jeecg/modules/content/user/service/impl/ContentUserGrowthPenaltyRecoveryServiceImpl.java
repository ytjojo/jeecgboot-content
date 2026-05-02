package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.Data;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserAppeal;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserBadgeGrant;
import org.jeecg.modules.content.user.entity.ContentUserGrowthLedger;
import org.jeecg.modules.content.user.entity.ContentUserGrowthPenaltyRecord;
import org.jeecg.modules.content.user.entity.ContentUserPointLedger;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserBadgeGrantMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthPenaltyRecordMapper;
import org.jeecg.modules.content.user.mapper.ContentUserPointLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.IContentUserGrowthPenaltyRecoveryService;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitRecoveryService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Service implementation for growth penalty recovery orchestration.
 */
@Service
public class ContentUserGrowthPenaltyRecoveryServiceImpl
    extends ServiceImpl<ContentUserGrowthPenaltyRecordMapper, ContentUserGrowthPenaltyRecord>
    implements IContentUserGrowthPenaltyRecoveryService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String APPEAL_TARGET_TYPE_GROWTH_PENALTY = "GROWTH_PENALTY";
    private static final String SOURCE_TYPE_PENALTY_RECOVER = "PENALTY_RECOVER";
    private static final String STATUS_PENDING_RECOVER = "PENDING_RECOVER";
    private static final String STATUS_RECOVERED = "RECOVERED";
    private static final String APPEAL_RESULT_APPROVED = "APPROVED";
    private static final String TRIGGER_APPEAL_APPROVED = "APPEAL_APPROVED";

    @Resource
    private ContentUserGrowthPenaltyRecordMapper growthPenaltyRecordMapper;

    @Resource
    private ContentUserPointLedgerMapper pointLedgerMapper;

    @Resource
    private ContentUserGrowthLedgerMapper growthLedgerMapper;

    @Resource
    private ContentUserBadgeGrantMapper badgeGrantMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserAuditLogMapper auditLogMapper;

    @Resource
    private IContentUserLevelBenefitRecoveryService levelBenefitRecoveryService;

    /**
     * Restores pending growth penalties when the appeal result is approved.
     */
    @Override
    public int recoverByAppeal(ContentUserAppeal appeal, String operatorUserId, Date executeTime, String reason) {
        if (appeal == null
            || !APPEAL_RESULT_APPROVED.equalsIgnoreCase(appeal.getResultStatus())
            || !APPEAL_TARGET_TYPE_GROWTH_PENALTY.equals(appeal.getTargetType())
            || appeal.getTargetId() == null
            || appeal.getTargetId().isBlank()) {
            return 0;
        }
        return recoverPendingRecords(
            Wrappers.<ContentUserGrowthPenaltyRecord>lambdaQuery()
                .eq(ContentUserGrowthPenaltyRecord::getId, appeal.getTargetId())
                .eq(ContentUserGrowthPenaltyRecord::getUserId, appeal.getUserId())
                .eq(ContentUserGrowthPenaltyRecord::getStatus, STATUS_PENDING_RECOVER),
            TRIGGER_APPEAL_APPROVED,
            operatorUserId,
            executeTime,
            reason,
            appeal.getId()
        );
    }

    /**
     * Restores pending growth penalties linked to the specified governance record.
     */
    @Override
    public int recoverByGovernanceRecord(ContentUserStatusRecord record, String operatorUserId, Date executeTime, String reason) {
        if (record == null || record.getId() == null) {
            return 0;
        }
        return recoverPendingRecords(
            Wrappers.<ContentUserGrowthPenaltyRecord>lambdaQuery()
                .eq(ContentUserGrowthPenaltyRecord::getGovernanceRecordId, record.getId())
                .eq(ContentUserGrowthPenaltyRecord::getStatus, STATUS_PENDING_RECOVER),
            "AUTO_EXPIRE_RECOVER",
            operatorUserId,
            executeTime,
            reason,
            null
        );
    }

    private int recoverPendingRecords(LambdaQueryWrapper<ContentUserGrowthPenaltyRecord> queryWrapper,
                                      String trigger,
                                      String operatorUserId,
                                      Date executeTime,
                                      String reason,
                                      String appealId) {
        List<ContentUserGrowthPenaltyRecord> records = growthPenaltyRecordMapper.selectList(queryWrapper);
        if (records == null || records.isEmpty()) {
            return 0;
        }
        int recoveredCount = 0;
        for (ContentUserGrowthPenaltyRecord record : records) {
            if (STATUS_RECOVERED.equals(record.getStatus())) {
                continue;
            }
            recoveredCount += recoverSingleRecord(record, trigger, operatorUserId, executeTime, reason, appealId);
        }
        return recoveredCount;
    }

    private int recoverSingleRecord(ContentUserGrowthPenaltyRecord record,
                                    String trigger,
                                    String operatorUserId,
                                    Date executeTime,
                                    String reason,
                                    String appealId) {
        ContentUserProfile profile = profileMapper.selectByUserId(record.getUserId());
        if (profile == null) {
            return 0;
        }
        GrowthPenaltySnapshot snapshot = parseSnapshot(record.getEffectSnapshotJson());
        int recoveredPoint = restorePoint(profile, record.getId(), trigger, snapshot.getPointDelta());
        int recoveredGrowth = restoreGrowth(profile, record.getId(), trigger, snapshot.getGrowthDelta());
        int recoveredBadgeCount = restoreBadges(snapshot.getBadgeEffects());
        int recoveredBenefitCount = levelBenefitRecoveryService == null
            ? 0
            : levelBenefitRecoveryService.recoverByPenaltyRecord(record, operatorUserId, executeTime, reason);

        profile.setPointBalance(Math.max(defaultZero(profile.getPointBalance()), 0));
        profile.setGrowthValue(Math.max(defaultZero(profile.getGrowthValue()), 0));
        profile.setLevel(calculateLevel(profile.getGrowthValue()));
        profileMapper.updateById(profile);

        record.setAppealId(appealId == null ? record.getAppealId() : appealId);
        record.setStatus(STATUS_RECOVERED);
        record.setRecoverTrigger(trigger);
        record.setRecoverReason(reason);
        record.setRecoveredBy(operatorUserId);
        record.setRecoveredAt(executeTime);
        growthPenaltyRecordMapper.updateById(record);

        if (auditLogMapper != null) {
            auditLogMapper.insert(ContentUserAuditLog.growthPenaltyRecovered(
                record.getUserId(),
                operatorUserId,
                trigger,
                record.getId(),
                recoveredPoint,
                recoveredGrowth,
                recoveredBadgeCount,
                recoveredBenefitCount
            ));
        }
        return 1;
    }

    private int restorePoint(ContentUserProfile profile, String bizId, String trigger, Integer pointDelta) {
        int penaltyPoint = defaultZero(pointDelta);
        if (penaltyPoint >= 0) {
            return 0;
        }
        int recoveredPoint = -penaltyPoint;
        int nextBalance = defaultZero(profile.getPointBalance()) + recoveredPoint;
        profile.setPointBalance(nextBalance);
        pointLedgerMapper.insert(new ContentUserPointLedger()
            .setUserId(profile.getUserId())
            .setSourceType(SOURCE_TYPE_PENALTY_RECOVER)
            .setBizId(bizId)
            .setPointDelta(recoveredPoint)
            .setBalanceAfter(nextBalance)
            .setRemark(trigger));
        return recoveredPoint;
    }

    private int restoreGrowth(ContentUserProfile profile, String bizId, String trigger, Integer growthDelta) {
        int penaltyGrowth = defaultZero(growthDelta);
        if (penaltyGrowth >= 0) {
            return 0;
        }
        int recoveredGrowth = -penaltyGrowth;
        int nextGrowth = defaultZero(profile.getGrowthValue()) + recoveredGrowth;
        profile.setGrowthValue(nextGrowth);
        growthLedgerMapper.insert(new ContentUserGrowthLedger()
            .setUserId(profile.getUserId())
            .setSourceType(SOURCE_TYPE_PENALTY_RECOVER)
            .setBizId(bizId)
            .setGrowthDelta(recoveredGrowth)
            .setGrowthAfter(nextGrowth)
            .setRemark(trigger));
        return recoveredGrowth;
    }

    private int restoreBadges(List<BadgeEffect> badgeEffects) {
        if (badgeEffects == null || badgeEffects.isEmpty()) {
            return 0;
        }
        int recoveredBadgeCount = 0;
        for (BadgeEffect badgeEffect : badgeEffects) {
            if (badgeEffect.getBadgeGrantId() == null) {
                continue;
            }
            ContentUserBadgeGrant badgeGrant = badgeGrantMapper.selectById(badgeEffect.getBadgeGrantId());
            if (badgeGrant == null) {
                continue;
            }
            boolean shouldUpdate = false;
            if (badgeEffect.getPreviousStatus() != null && !badgeEffect.getPreviousStatus().equals(badgeGrant.getStatus())) {
                badgeGrant.setStatus(badgeEffect.getPreviousStatus());
                shouldUpdate = true;
            }
            if (badgeEffect.getPreviousDisplaying() != null && !badgeEffect.getPreviousDisplaying().equals(badgeGrant.getDisplaying())) {
                badgeGrant.setDisplaying(badgeEffect.getPreviousDisplaying());
                shouldUpdate = true;
            }
            if (shouldUpdate) {
                badgeGrant.setRecycledAt(null);
                badgeGrantMapper.updateById(badgeGrant);
            }
            recoveredBadgeCount++;
        }
        return recoveredBadgeCount;
    }

    private GrowthPenaltySnapshot parseSnapshot(String effectSnapshotJson) {
        if (effectSnapshotJson == null || effectSnapshotJson.isBlank()) {
            return new GrowthPenaltySnapshot();
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(effectSnapshotJson);
            GrowthPenaltySnapshot snapshot = new GrowthPenaltySnapshot();
            JsonNode pointEffectNode = root.path("pointEffect");
            JsonNode growthEffectNode = root.path("growthEffect");
            snapshot.setPointDelta(pointEffectNode.isMissingNode()
                ? root.path("pointDelta").asInt(0)
                : pointEffectNode.path("delta").asInt(0));
            snapshot.setGrowthDelta(growthEffectNode.isMissingNode()
                ? root.path("growthDelta").asInt(0)
                : growthEffectNode.path("delta").asInt(0));
            JsonNode badgeEffectsNode = root.path("badgeEffects");
            List<BadgeEffect> badgeEffects = new ArrayList<>();
            if (badgeEffectsNode.isArray()) {
                for (JsonNode badgeEffectNode : badgeEffectsNode) {
                    BadgeEffect badgeEffect = new BadgeEffect();
                    badgeEffect.setBadgeGrantId(textValueOrNull(badgeEffectNode, "badgeGrantId"));
                    badgeEffect.setPreviousStatus(textValueOrNull(badgeEffectNode, "previousStatus"));
                    badgeEffect.setPreviousDisplaying(booleanValueOrNull(badgeEffectNode, "previousDisplaying"));
                    badgeEffects.add(badgeEffect);
                }
            }
            snapshot.setBadgeEffects(badgeEffects);
            return snapshot;
        } catch (IOException ex) {
            throw new JeecgBootException("成长处罚快照解析失败");
        }
    }

    private String textValueOrNull(JsonNode node, String fieldName) {
        JsonNode valueNode = node.get(fieldName);
        return valueNode == null || valueNode.isNull() ? null : valueNode.asText();
    }

    private Boolean booleanValueOrNull(JsonNode node, String fieldName) {
        JsonNode valueNode = node.get(fieldName);
        return valueNode == null || valueNode.isNull() ? null : valueNode.asBoolean();
    }

    private int calculateLevel(Integer growthValue) {
        int safeGrowth = Math.max(defaultZero(growthValue), 0);
        return Math.max(1, safeGrowth / 100 + 1);
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    @Data
    private static final class GrowthPenaltySnapshot {
        private Integer pointDelta;
        private Integer growthDelta;
        private List<BadgeEffect> badgeEffects = List.of();
    }

    @Data
    private static final class BadgeEffect {
        private String badgeGrantId;
        private String previousStatus;
        private Boolean previousDisplaying;
    }
}
