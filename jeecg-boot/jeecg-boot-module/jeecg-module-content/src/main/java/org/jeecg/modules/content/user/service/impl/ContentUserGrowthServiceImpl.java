package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.constant.ContentUserCacheConstant;
import org.jeecg.modules.content.user.constant.ContentUserRewardSourceTypeConstant;
import org.jeecg.modules.content.user.dto.ContentUserRewardEventDTO;
import org.jeecg.modules.content.user.dto.ContentUserRewardResultDTO;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserGrowthLedger;
import org.jeecg.modules.content.user.entity.ContentUserPointLedger;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserRewardEvent;
import org.jeecg.modules.content.user.entity.ContentUserRewardRule;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserPointLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRewardEventMapper;
import org.jeecg.modules.content.user.service.IContentUserGrowthDecayStateService;
import org.jeecg.modules.content.user.service.IContentUserGrowthService;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitService;
import org.jeecg.modules.content.user.service.IContentUserLevelConfigService;
import org.jeecg.modules.content.user.service.IContentUserRewardRuleService;
import org.jeecg.modules.content.user.vo.ContentUserGrowthVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service implementation for content user growth.
 */
@Service
public class ContentUserGrowthServiceImpl implements IContentUserGrowthService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_SKIPPED = "SKIPPED";
    private static final String SKIP_NO_RULE = "NO_ENABLED_RULE";
    private static final String SKIP_DAILY_CAP = "DAILY_CAP_REACHED";
    private static final int MAX_DIRECT_AWARD = 100_000;

    @Resource
    private ContentUserPointLedgerMapper pointLedgerMapper;

    @Resource
    private ContentUserGrowthLedgerMapper growthLedgerMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserAuditLogMapper auditLogMapper;

    @Resource
    private ContentUserRewardEventMapper rewardEventMapper;

    @Resource
    private IContentUserRewardRuleService rewardRuleService;

    @Resource
    private IContentUserLevelBenefitService levelBenefitService;

    @Resource
    private IContentUserLevelConfigService levelConfigService;

    @Resource
    private IContentUserGrowthDecayStateService growthDecayStateService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Records point and growth ledger changes for a user behavior.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordBehavior(String userId, String sourceType, int pointDelta, int growthDelta) {
        reward(new ContentUserRewardEventDTO()
            .setUserId(userId)
            .setSourceType(sourceType)
            .setEventId("LEGACY_" + System.currentTimeMillis() + "_" + System.nanoTime())
            .setPointAmount(pointDelta)
            .setGrowthAmount(growthDelta)
            .setLegacyDirectAward(Boolean.TRUE));
    }

    /**
     * Processes one configured reward event with idempotency and daily caps.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserRewardResultDTO reward(ContentUserRewardEventDTO event) {
        validateEvent(event);
        ContentUserRewardEvent existingEvent = selectEvent(event.getEventId());
        if (existingEvent != null) {
            return toResult(existingEvent, true);
        }
        if (!tryLockEvent(event.getEventId())) {
            ContentUserRewardEvent lockedExistingEvent = selectEvent(event.getEventId());
            if (lockedExistingEvent != null) {
                return toResult(lockedExistingEvent, true);
            }
            return ContentUserRewardResultDTO.of(event.getEventId(), event.getUserId(), event.getSourceType(), null,
                0, 0, STATUS_SKIPPED, "EVENT_LOCKED", true);
        }

        RewardDecision decision = resolveRewardDecision(event);
        ContentUserRewardEvent rewardEvent = buildRewardEvent(event, decision);
        if (decision.shouldAward()) {
            SummaryAfter summaryAfter = updateProfileSummary(event.getUserId(), decision.pointDelta(), decision.growthDelta());
            insertLedgers(event, decision, summaryAfter);
            markUserActiveForDecay(event.getUserId(), summaryAfter.growthValue());
            if (auditLogMapper != null) {
                auditLogMapper.insert(ContentUserAuditLog.behaviorAwarded(event.getUserId(), event.getSourceType(),
                    decision.pointDelta(), decision.growthDelta()));
            }
        }
        if (rewardEventMapper != null) {
            rewardEventMapper.insert(rewardEvent);
        }
        return toResult(rewardEvent, false);
    }

    /**
     * Gets the point, growth, and level summary for the target user.
     */
    @Override
    public ContentUserGrowthVO getGrowthSummary(String userId) {
        ContentUserProfile profile = profileMapper.selectOne(
            Wrappers.<ContentUserProfile>lambdaQuery().eq(ContentUserProfile::getUserId, userId).last("limit 1")
        );
        if (profile == null) {
            return new ContentUserGrowthVO()
                .setUserId(userId)
                .setPointBalance(0)
                .setGrowthValue(0)
                .setLevel(1)
                .setLevelBenefitSummary(levelBenefitService == null ? null : levelBenefitService.getBenefitSummary(userId));
        }
        return new ContentUserGrowthVO()
            .setUserId(userId)
            .setPointBalance(defaultZero(profile.getPointBalance()))
            .setGrowthValue(defaultZero(profile.getGrowthValue()))
            .setLevel(defaultLevel(profile.getLevel(), profile.getGrowthValue()))
            .setLevelBenefitSummary(levelBenefitService == null ? null : levelBenefitService.getBenefitSummary(userId));
    }

    private SummaryAfter updateProfileSummary(String userId, int pointDelta, int growthDelta) {
        if (profileMapper == null) {
            return new SummaryAfter(Math.max(pointDelta, 0), Math.max(growthDelta, 0));
        }
        ContentUserProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            return new SummaryAfter(Math.max(pointDelta, 0), Math.max(growthDelta, 0));
        }
        int nextPointBalance = defaultZero(profile.getPointBalance()) + pointDelta;
        int nextGrowthValue = defaultZero(profile.getGrowthValue()) + growthDelta;
        int beforeLevel = defaultLevel(profile.getLevel(), profile.getGrowthValue());
        profile.setPointBalance(Math.max(nextPointBalance, 0));
        profile.setGrowthValue(Math.max(nextGrowthValue, 0));
        profile.setLevel(calculateLevel(profile.getGrowthValue()));
        profileMapper.updateById(profile);
        notifyLevelUp(userId, growthDelta, beforeLevel, profile.getLevel(), profile.getGrowthValue());
        return new SummaryAfter(profile.getPointBalance(), profile.getGrowthValue());
    }

    private void markUserActiveForDecay(String userId, int currentGrowthValue) {
        if (growthDecayStateService == null) {
            return;
        }
        // 奖励事件代表用户重新活跃，衰减状态由专门服务判断是否清除保护。
        growthDecayStateService.markUserActive(userId, new java.util.Date(), currentGrowthValue);
    }

    private void notifyLevelUp(String userId, int growthDelta, int beforeLevel, int afterLevel, int growthValue) {
        if (growthDelta <= 0 || afterLevel <= beforeLevel || auditLogMapper == null) {
            return;
        }
        auditLogMapper.insert(ContentUserAuditLog.levelUp(userId, beforeLevel, afterLevel, growthValue));
    }

    private void validateEvent(ContentUserRewardEventDTO event) {
        if (event == null || isBlank(event.getUserId()) || isBlank(event.getSourceType()) || isBlank(event.getEventId())) {
            throw new JeecgBootException("奖励事件参数不能为空");
        }
        if (!Boolean.TRUE.equals(event.getLegacyDirectAward())
            && !ContentUserRewardSourceTypeConstant.SUPPORTED_TYPES.contains(event.getSourceType())) {
            throw new JeecgBootException("奖励来源类型不支持");
        }
        if (event.getPointAmount() != null && (event.getPointAmount() < 0 || event.getPointAmount() > MAX_DIRECT_AWARD)) {
            throw new JeecgBootException("奖励积分金额不合法");
        }
        if (event.getGrowthAmount() != null && (event.getGrowthAmount() < 0 || event.getGrowthAmount() > MAX_DIRECT_AWARD)) {
            throw new JeecgBootException("奖励成长值金额不合法");
        }
    }

    private RewardDecision resolveRewardDecision(ContentUserRewardEventDTO event) {
        String bucket = normalizeDailyBucket(event.getDailyBucket());
        if (Boolean.TRUE.equals(event.getLegacyDirectAward())) {
            return new RewardDecision(null, defaultZero(event.getPointAmount()), defaultZero(event.getGrowthAmount()), bucket, STATUS_SUCCESS, null);
        }
        Optional<ContentUserRewardRule> ruleOptional = rewardRuleService == null
            ? Optional.empty()
            : rewardRuleService.getEnabledRule(event.getSourceType());
        if (ruleOptional.isEmpty()) {
            return new RewardDecision(null, 0, 0, bucket, STATUS_SKIPPED, SKIP_NO_RULE);
        }
        ContentUserRewardRule rule = ruleOptional.get();
        int pointDelta = defaultZero(rule.getPointAmount());
        int growthDelta = defaultZero(rule.getGrowthAmount());
        if (exceedsDailyCap(event.getUserId(), event.getSourceType(), bucket, pointDelta, rule.getDailyPointCap())
            || exceedsDailyCap(event.getUserId(), event.getSourceType() + ":growth", bucket, growthDelta, rule.getDailyGrowthCap())) {
            return new RewardDecision(rule, 0, 0, bucket, STATUS_SKIPPED, SKIP_DAILY_CAP);
        }
        return new RewardDecision(rule, pointDelta, growthDelta, bucket, STATUS_SUCCESS, null);
    }

    private boolean exceedsDailyCap(String userId, String sourceType, String bucket, int delta, Integer cap) {
        if (cap == null || cap <= 0 || delta <= 0 || redisTemplate == null) {
            return false;
        }
        String key = ContentUserCacheConstant.GROWTH_DAILY_CAP_PREFIX + sourceType + ":" + userId + ":" + bucket;
        Long value = redisTemplate.opsForValue().increment(key, delta);
        if (value != null && value == delta) {
            redisTemplate.expire(key, 1, TimeUnit.DAYS);
        }
        return value != null && value > cap;
    }

    private boolean tryLockEvent(String eventId) {
        if (redisTemplate == null) {
            return true;
        }
        var valueOperations = redisTemplate.opsForValue();
        if (valueOperations == null) {
            return true;
        }
        Boolean locked = valueOperations
            .setIfAbsent(ContentUserCacheConstant.GROWTH_EVENT_LOCK_PREFIX + eventId, "1", 10, TimeUnit.MINUTES);
        return Boolean.TRUE.equals(locked);
    }

    private ContentUserRewardEvent selectEvent(String eventId) {
        if (rewardEventMapper == null) {
            return null;
        }
        return rewardEventMapper.selectOne(
            Wrappers.<ContentUserRewardEvent>lambdaQuery().eq(ContentUserRewardEvent::getEventId, eventId).last("limit 1")
        );
    }

    private ContentUserRewardEvent buildRewardEvent(ContentUserRewardEventDTO event, RewardDecision decision) {
        return new ContentUserRewardEvent()
            .setEventId(event.getEventId())
            .setUserId(event.getUserId())
            .setSourceType(event.getSourceType())
            .setRuleCode(decision.ruleCode())
            .setPointDelta(decision.pointDelta())
            .setGrowthDelta(decision.growthDelta())
            .setDailyBucket(decision.dailyBucket())
            .setProcessStatus(decision.processStatus())
            .setSkipReason(decision.skipReason());
    }

    private void insertLedgers(ContentUserRewardEventDTO event, RewardDecision decision, SummaryAfter summaryAfter) {
        String snapshot = decision.ruleSnapshot();
        if (decision.pointDelta() != 0) {
            pointLedgerMapper.insert(ContentUserPointLedger.of(event.getUserId(), event.getSourceType(), event.getBizId(),
                    decision.pointDelta(), "BEHAVIOR_AWARD")
                .setSourceDescription(decision.sourceDescription())
                .setEventId(event.getEventId())
                .setRuleSnapshotJson(snapshot)
                .setDailyBucket(decision.dailyBucket())
                .setBalanceAfter(summaryAfter.pointBalance()));
        }
        if (decision.growthDelta() != 0) {
            growthLedgerMapper.insert(ContentUserGrowthLedger.of(event.getUserId(), event.getSourceType(), event.getBizId(),
                    decision.growthDelta(), "BEHAVIOR_AWARD")
                .setSourceDescription(decision.sourceDescription())
                .setEventId(event.getEventId())
                .setRuleSnapshotJson(snapshot)
                .setGrowthAfter(summaryAfter.growthValue()));
        }
    }

    private ContentUserRewardResultDTO toResult(ContentUserRewardEvent event, boolean duplicate) {
        return ContentUserRewardResultDTO.of(event.getEventId(), event.getUserId(), event.getSourceType(), event.getRuleCode(),
            event.getPointDelta(), event.getGrowthDelta(), event.getProcessStatus(), event.getSkipReason(), duplicate);
    }

    private String normalizeDailyBucket(String dailyBucket) {
        if (!isBlank(dailyBucket)) {
            return dailyBucket;
        }
        return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private int defaultLevel(Integer level, Integer growthValue) {
        return level == null ? calculateLevel(defaultZero(growthValue)) : level;
    }

    private int calculateLevel(Integer growthValue) {
        if (levelConfigService != null) {
            return levelConfigService.calculateLevel(growthValue);
        }
        int safeGrowth = Math.max(defaultZero(growthValue), 0);
        return Math.max(1, safeGrowth / 100 + 1);
    }

    private record SummaryAfter(int pointBalance, int growthValue) {
    }

    private record RewardDecision(ContentUserRewardRule rule, int pointDelta, int growthDelta, String dailyBucket,
                                  String processStatus, String skipReason) {

        private boolean shouldAward() {
            return STATUS_SUCCESS.equals(processStatus) && (pointDelta != 0 || growthDelta != 0);
        }

        private String ruleCode() {
            return rule == null ? null : rule.getRuleCode();
        }

        private String sourceDescription() {
            return rule == null ? null : rule.getRuleDescription();
        }

        private String ruleSnapshot() {
            if (rule == null) {
                return null;
            }
            return "{\"ruleCode\":\"" + rule.getRuleCode() + "\",\"pointAmount\":" + defaultZero(rule.getPointAmount())
                + ",\"growthAmount\":" + defaultZero(rule.getGrowthAmount()) + "}";
        }

        private int defaultZero(Integer value) {
            return value == null ? 0 : value;
        }
    }
}
