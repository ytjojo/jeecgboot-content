package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserGrowthDecayState;
import org.jeecg.modules.content.user.entity.ContentUserGrowthLedger;
import org.jeecg.modules.content.user.entity.ContentUserLevelConfig;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthDecayStateMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.IContentUserGrowthDecayStateService;
import org.jeecg.modules.content.user.service.IContentUserLevelConfigService;
import org.jeecg.modules.content.user.vo.ContentUserGrowthDecayRuleVO;
import org.jeecg.modules.content.user.vo.ContentUserGrowthDecayStatusVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 内容社区成长值衰减状态服务实现。
 */
@Service
public class ContentUserGrowthDecayStateServiceImpl
    extends ServiceImpl<ContentUserGrowthDecayStateMapper, ContentUserGrowthDecayState>
    implements IContentUserGrowthDecayStateService {

    private static final String STATUS_NORMAL = "NORMAL";
    private static final String STATUS_PROTECTING = "PROTECTING";
    private static final String SOURCE_TYPE_GROWTH_DECAY = "GROWTH_DECAY";
    private static final int DEFAULT_INACTIVE_DAYS = 30;
    private static final int DEFAULT_PROTECTION_DAYS = 7;
    private static final int MAX_RULE_DESCRIPTION_LENGTH = 512;
    private static final BigDecimal DEFAULT_DECAY_RATE = new BigDecimal("0.05");
    private static final BigDecimal MAX_DECAY_RATE = new BigDecimal("0.50");

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserGrowthLedgerMapper growthLedgerMapper;

    @Resource
    private ContentUserDeviceSessionMapper deviceSessionMapper;

    @Resource
    private ContentUserAuditLogMapper auditLogMapper;

    @Resource
    private IContentUserLevelConfigService levelConfigService;

    @Override
    public ContentUserGrowthDecayRuleVO getDecayRule() {
        return defaultRule();
    }

    @Override
    public ContentUserGrowthDecayRuleVO validateDecayRule(ContentUserGrowthDecayRuleVO rule) {
        ContentUserGrowthDecayRuleVO target = rule == null ? defaultRule() : rule;
        if (target.getEnabled() == null) {
            throw new JeecgBootException("成长值衰减启用状态不能为空");
        }
        if (target.getInactiveDays() == null || target.getInactiveDays() < 1) {
            throw new JeecgBootException("成长值衰减未活跃天数不合法");
        }
        if (target.getDecayRate() == null
            || target.getDecayRate().compareTo(BigDecimal.ZERO) < 0
            || target.getDecayRate().compareTo(MAX_DECAY_RATE) > 0) {
            throw new JeecgBootException("成长值衰减比例不合法");
        }
        if (target.getProtectionDays() == null || target.getProtectionDays() < 0) {
            throw new JeecgBootException("成长值降级保护天数不合法");
        }
        if (target.getRuleDescription() != null
            && target.getRuleDescription().length() > MAX_RULE_DESCRIPTION_LENGTH) {
            throw new JeecgBootException("成长值衰减规则说明过长");
        }
        return target;
    }

    @Override
    public List<ContentUserProfile> listDecayCandidates(Date runTime) {
        return listDecayCandidates(runTime, defaultRule());
    }

    @Override
    public List<ContentUserProfile> listDecayCandidates(Date runTime, ContentUserGrowthDecayRuleVO rule) {
        Date now = resolveRunTime(runTime);
        ContentUserGrowthDecayRuleVO validRule = validateDecayRule(rule);
        if (!Boolean.TRUE.equals(validRule.getEnabled()) || profileMapper == null) {
            return List.of();
        }
        Date cutoff = daysBefore(now, validRule.getInactiveDays());
        List<ContentUserProfile> profiles = profileMapper.selectList(
            Wrappers.<ContentUserProfile>lambdaQuery().gt(ContentUserProfile::getGrowthValue, 0)
        );
        if (profiles == null || profiles.isEmpty()) {
            return List.of();
        }
        return profiles.stream()
            .filter(profile -> profile != null && profile.getUserId() != null)
            .filter(profile -> {
                Date lastActiveTime = resolveLastActiveTime(profile.getUserId(), null);
                // 第 30 天仍不衰减，必须早于阈值时间才进入第 31 天候选。
                return lastActiveTime != null && lastActiveTime.before(cutoff);
            })
            .toList();
    }

    @Override
    public int executeDecay(Date runTime) {
        return executeDecay(runTime, defaultRule());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int executeDecay(Date runTime, ContentUserGrowthDecayRuleVO rule) {
        Date now = resolveRunTime(runTime);
        ContentUserGrowthDecayRuleVO validRule = validateDecayRule(rule);
        if (!Boolean.TRUE.equals(validRule.getEnabled())) {
            return 0;
        }
        int affected = downgradeExpiredProtections(now);
        for (ContentUserProfile profile : listDecayCandidates(now, validRule)) {
            if (profile == null || profile.getUserId() == null || alreadyDecayedToday(profile.getUserId(), now)) {
                continue;
            }
            affected += applyDecay(profile, now, validRule);
        }
        return affected;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markUserActive(String userId, Date activeTime, Integer currentGrowthValue) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        ContentUserGrowthDecayState state = selectState(userId);
        if (state == null) {
            state = new ContentUserGrowthDecayState()
                .setUserId(userId)
                .setStatus(STATUS_NORMAL)
                .setDecayCount(0);
            state.setId(UUIDGenerator.generate());
            state.setCreateTime(resolveRunTime(activeTime));
            state.setLastActiveTime(resolveRunTime(activeTime));
            baseMapper.insert(state);
            return;
        }
        state.setLastActiveTime(resolveRunTime(activeTime));
        if (STATUS_PROTECTING.equals(state.getStatus())
            && currentGrowthValue != null
            && currentGrowthValue >= currentLevelThreshold(userId, currentGrowthValue)) {
            state.setStatus(STATUS_NORMAL);
            state.setProtectionStartedAt(null);
            state.setProtectionUntil(null);
        }
        baseMapper.updateById(state);
    }

    @Override
    public ContentUserGrowthDecayStatusVO getDecayStatus(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new JeecgBootException("用户ID不能为空");
        }
        ContentUserProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new JeecgBootException("用户不存在");
        }

        ContentUserGrowthDecayState state = selectState(userId);

        Date lastActive = state != null ? state.getLastActiveTime() : null;
        int inactiveDays = 0;
        if (lastActive != null) {
            long diffMs = System.currentTimeMillis() - lastActive.getTime();
            inactiveDays = (int) (diffMs / (24L * 60 * 60 * 1000));
        }

        String status = state != null ? state.getStatus() : STATUS_NORMAL;

        return new ContentUserGrowthDecayStatusVO()
            .setStatus(status)
            .setInactiveDays(inactiveDays)
            .setProtectionUntil(state != null ? state.getProtectionUntil() : null)
            .setCurrentLevel(profile.getLevel())
            .setCurrentGrowthValue(profile.getGrowthValue())
            .setLastActiveTime(lastActive)
            .setDecayCount(state != null ? defaultZero(state.getDecayCount()) : 0);
    }

    private int applyDecay(ContentUserProfile profile, Date now, ContentUserGrowthDecayRuleVO rule) {
        int beforeGrowth = defaultZero(profile.getGrowthValue());
        int actualDelta = calculateDecayDelta(beforeGrowth, rule.getDecayRate());
        if (actualDelta <= 0) {
            return 0;
        }
        int afterGrowth = Math.max(beforeGrowth - actualDelta, 0);
        int beforeLevel = defaultLevel(profile.getLevel(), beforeGrowth);
        profile.setGrowthValue(afterGrowth);
        growthLedgerMapper.insert(ContentUserGrowthLedger.of(profile.getUserId(), SOURCE_TYPE_GROWTH_DECAY,
                "DECAY_" + toLocalDate(now), -actualDelta, "连续未活跃成长值衰减")
            .setGrowthAfter(afterGrowth)
            .setRuleSnapshotJson(ruleSnapshot(rule)));
        ContentUserGrowthDecayState state = ensureState(profile.getUserId(), now);
        state.setLastActiveTime(resolveLastActiveTime(profile.getUserId(), state));
        state.setLastDecayTime(now);
        state.setDecayCount(defaultZero(state.getDecayCount()) + 1);
        state.setRuleSnapshotJson(ruleSnapshot(rule));
        if (afterGrowth < levelThreshold(beforeLevel)) {
            state.setStatus(STATUS_PROTECTING);
            state.setProtectionStartedAt(now);
            state.setProtectionUntil(daysAfter(now, rule.getProtectionDays()));
        } else {
            state.setStatus(STATUS_NORMAL);
        }
        profileMapper.updateById(profile);
        baseMapper.updateById(state);
        return 1;
    }

    private int downgradeExpiredProtections(Date now) {
        if (profileMapper == null || baseMapper == null) {
            return 0;
        }
        List<ContentUserGrowthDecayState> states = baseMapper.selectList(
            Wrappers.<ContentUserGrowthDecayState>lambdaQuery()
                .eq(ContentUserGrowthDecayState::getStatus, STATUS_PROTECTING)
                .le(ContentUserGrowthDecayState::getProtectionUntil, now)
        );
        if (states == null || states.isEmpty()) {
            return 0;
        }
        int affected = 0;
        for (ContentUserGrowthDecayState state : states) {
            ContentUserProfile profile = profileMapper.selectByUserId(state.getUserId());
            if (profile == null) {
                continue;
            }
            int currentLevel = defaultLevel(profile.getLevel(), profile.getGrowthValue());
            int growth = defaultZero(profile.getGrowthValue());
            if (growth < levelThreshold(currentLevel)) {
                int targetLevel = calculateLevel(growth);
                if (targetLevel < currentLevel) {
                    profile.setLevel(targetLevel);
                    profileMapper.updateById(profile);
                    if (auditLogMapper != null) {
                        auditLogMapper.insert(ContentUserAuditLog.levelDown(state.getUserId(), currentLevel,
                            targetLevel, growth));
                    }
                    affected++;
                }
            }
            state.setStatus(STATUS_NORMAL);
            state.setProtectionStartedAt(null);
            state.setProtectionUntil(null);
            baseMapper.updateById(state);
        }
        return affected;
    }

    private ContentUserGrowthDecayState ensureState(String userId, Date now) {
        ContentUserGrowthDecayState state = selectState(userId);
        if (state != null) {
            return state;
        }
        state = new ContentUserGrowthDecayState()
            .setUserId(userId)
            .setStatus(STATUS_NORMAL)
            .setDecayCount(0);
        state.setId(UUIDGenerator.generate());
        state.setCreateTime(now);
        baseMapper.insert(state);
        return state;
    }

    private ContentUserGrowthDecayState selectState(String userId) {
        if (baseMapper == null) {
            return null;
        }
        return baseMapper.selectOne(
            Wrappers.<ContentUserGrowthDecayState>lambdaQuery()
                .eq(ContentUserGrowthDecayState::getUserId, userId)
                .last("limit 1")
        );
    }

    private boolean alreadyDecayedToday(String userId, Date now) {
        ContentUserGrowthDecayState state = selectState(userId);
        return state != null
            && state.getLastDecayTime() != null
            && toLocalDate(state.getLastDecayTime()).equals(toLocalDate(now));
    }

    private Date resolveLastActiveTime(String userId, ContentUserGrowthDecayState state) {
        Date deviceActiveTime = deviceSessionMapper == null ? null : deviceSessionMapper.selectLatestActiveTimeByUserId(userId);
        if (deviceActiveTime != null) {
            return deviceActiveTime;
        }
        ContentUserGrowthDecayState target = state == null ? selectState(userId) : state;
        return target == null ? null : target.getLastActiveTime();
    }

    private int calculateDecayDelta(int growthValue, BigDecimal decayRate) {
        if (growthValue <= 0 || decayRate == null || decayRate.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        int delta = BigDecimal.valueOf(growthValue).multiply(decayRate).setScale(0, RoundingMode.DOWN).intValue();
        return Math.min(growthValue, Math.max(delta, 1));
    }

    private int currentLevelThreshold(String userId, int currentGrowthValue) {
        ContentUserProfile profile = profileMapper == null ? null : profileMapper.selectByUserId(userId);
        int level = profile == null ? calculateLevel(currentGrowthValue) : defaultLevel(profile.getLevel(), currentGrowthValue);
        return levelThreshold(level);
    }

    private int levelThreshold(int level) {
        if (level <= 1) {
            return 0;
        }
        if (levelConfigService != null) {
            List<ContentUserLevelConfig> configs = levelConfigService.listValidEnabledLevels();
            if (configs != null && !configs.isEmpty()) {
                return configs.stream()
                    .filter(item -> item.getLevel() != null && item.getLevel() == level)
                    .map(ContentUserLevelConfig::getGrowthThreshold)
                    .filter(value -> value != null)
                    .findFirst()
                    .orElse((level - 1) * 100);
            }
        }
        return (level - 1) * 100;
    }

    private int calculateLevel(Integer growthValue) {
        if (levelConfigService != null) {
            return levelConfigService.calculateLevel(growthValue);
        }
        int safeGrowth = Math.max(defaultZero(growthValue), 0);
        return Math.max(1, safeGrowth / 100 + 1);
    }

    private int defaultLevel(Integer level, Integer growthValue) {
        return level == null ? calculateLevel(growthValue) : level;
    }

    private ContentUserGrowthDecayRuleVO defaultRule() {
        return new ContentUserGrowthDecayRuleVO()
            .setEnabled(Boolean.TRUE)
            .setInactiveDays(DEFAULT_INACTIVE_DAYS)
            .setDecayRate(DEFAULT_DECAY_RATE)
            .setProtectionDays(DEFAULT_PROTECTION_DAYS)
            .setRuleDescription("连续 30 天未登录后按 5% 衰减成长值，低于当前等级阈值时进入 7 天降级保护期");
    }

    private String ruleSnapshot(ContentUserGrowthDecayRuleVO rule) {
        return "{\"inactiveDays\":" + rule.getInactiveDays()
            + ",\"decayRate\":" + rule.getDecayRate()
            + ",\"protectionDays\":" + rule.getProtectionDays()
            + ",\"enabled\":" + rule.getEnabled() + "}";
    }

    private Date resolveRunTime(Date runTime) {
        return runTime == null ? new Date() : runTime;
    }

    private Date daysBefore(Date time, int days) {
        return Date.from(time.toInstant().minusSeconds(days * 24L * 60L * 60L));
    }

    private Date daysAfter(Date time, int days) {
        return Date.from(time.toInstant().plusSeconds(days * 24L * 60L * 60L));
    }

    private LocalDate toLocalDate(Date time) {
        return time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
