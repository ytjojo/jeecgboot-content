package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserLevelBenefitConfig;
import org.jeecg.modules.content.user.entity.ContentUserLevelBenefitPenaltyRecord;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.mapper.ContentUserLevelBenefitConfigMapper;
import org.jeecg.modules.content.user.mapper.ContentUserLevelBenefitPenaltyRecordMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitService;
import org.jeecg.modules.content.user.vo.ContentUserDistributionWeightVO;
import org.jeecg.modules.content.user.vo.ContentUserLevelBenefitSummaryVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Service implementation for runtime level benefit evaluation.
 */
@Service
public class ContentUserLevelBenefitServiceImpl implements IContentUserLevelBenefitService {

    private static final int DEFAULT_UPLOAD_LIMIT_MB = 100;
    private static final int ENHANCED_UPLOAD_LIMIT_MB = 500;
    private static final int DEFAULT_TOPIC_QUOTA = 10;
    private static final int ENHANCED_TOPIC_QUOTA = 30;
    private static final int DEFAULT_SUPPORT_PRIORITY = 0;
    private static final int ENHANCED_SUPPORT_PRIORITY = 1;
    private static final BigDecimal DEFAULT_DISTRIBUTION_WEIGHT = BigDecimal.ONE;
    private static final BigDecimal MAX_DISTRIBUTION_WEIGHT = new BigDecimal("1.20");

    private static final String BENEFIT_UPLOAD_EXPANDED = "UPLOAD_EXPANDED";
    private static final String BENEFIT_HD_VIDEO = "HD_VIDEO";
    private static final String BENEFIT_TOPIC_QUOTA_EXPANDED = "TOPIC_QUOTA_EXPANDED";
    private static final String BENEFIT_UPLOAD_LIMIT_MB = "UPLOAD_SIZE_LIMIT_MB";
    private static final String BENEFIT_TOPIC_QUOTA = "TOPIC_QUOTA";
    private static final String BENEFIT_SUPPORT_PRIORITY = "SUPPORT_PRIORITY";
    private static final String BENEFIT_LEVEL_BADGE_STYLE = "LEVEL_BADGE_STYLE";
    private static final String BENEFIT_COMMENT_EFFECT = "COMMENT_EFFECT";
    private static final String BENEFIT_DISTRIBUTION_WEIGHT = "DISTRIBUTION_WEIGHT";
    private static final String STATUS_PENDING_RECOVER = "PENDING_RECOVER";
    private static final String STATUS_RECOVERED = "RECOVERED";
    private static final Set<String> SUPPORTED_CONFIG_KEYS = Set.of(
        BENEFIT_UPLOAD_LIMIT_MB,
        BENEFIT_UPLOAD_EXPANDED,
        BENEFIT_HD_VIDEO,
        BENEFIT_TOPIC_QUOTA,
        BENEFIT_TOPIC_QUOTA_EXPANDED,
        BENEFIT_SUPPORT_PRIORITY,
        BENEFIT_LEVEL_BADGE_STYLE,
        BENEFIT_COMMENT_EFFECT,
        BENEFIT_DISTRIBUTION_WEIGHT
    );

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserLevelBenefitPenaltyRecordMapper levelBenefitPenaltyRecordMapper;

    @Resource
    private ContentUserLevelBenefitConfigMapper levelBenefitConfigMapper;

    /**
     * Builds runtime benefit summary for the target user.
     */
    @Override
    public ContentUserLevelBenefitSummaryVO getBenefitSummary(String userId) {
        ContentUserProfile profile = profileMapper == null ? null : profileMapper.selectByUserId(userId);
        BenefitState state = loadBenefitState(userId);
        RuntimeBenefit runtimeBenefit = resolveRuntimeBenefit(profile);
        Set<String> enabledCodes = new LinkedHashSet<>(runtimeBenefit.enabledConfigKeys);
        enabledCodes.addAll(state.enabledCodes);
        return new ContentUserLevelBenefitSummaryVO()
            .setUploadSizeLimitMb(resolveUploadLimit(runtimeBenefit, state))
            .setHdVideoEnabled(resolveHdVideoEnabled(runtimeBenefit, state))
            .setTopicQuota(resolveTopicQuota(runtimeBenefit, state))
            .setSupportPriority(resolveSupportPriority(runtimeBenefit))
            .setLevelBadgeStyleKey(runtimeBenefit.levelBadgeStyleKey)
            .setCommentEffectKey(runtimeBenefit.commentEffectKey)
            .setEnabledBenefitCodes(new ArrayList<>(enabledCodes));
    }

    /**
     * Returns whether the target benefit is currently available.
     */
    @Override
    public boolean hasEnabledBenefit(String userId, String benefitCode) {
        if (!StringUtils.hasText(benefitCode)) {
            return false;
        }
        ContentUserProfile profile = profileMapper == null ? null : profileMapper.selectByUserId(userId);
        BenefitState state = loadBenefitState(userId);
        if (state.disabledCodes.contains(benefitCode)) {
            return false;
        }
        if (state.enabledCodes.contains(benefitCode)) {
            return true;
        }
        RuntimeBenefit runtimeBenefit = resolveRuntimeBenefit(profile);
        if (BENEFIT_UPLOAD_EXPANDED.equals(benefitCode)) {
            return runtimeBenefit.uploadSizeLimitMb > DEFAULT_UPLOAD_LIMIT_MB;
        }
        if (BENEFIT_HD_VIDEO.equals(benefitCode)) {
            return runtimeBenefit.hdVideoEnabled;
        }
        if (BENEFIT_TOPIC_QUOTA_EXPANDED.equals(benefitCode)) {
            return runtimeBenefit.topicQuota > DEFAULT_TOPIC_QUOTA;
        }
        return runtimeBenefit.enabledConfigKeys.contains(benefitCode);
    }

    /**
     * Returns whether the target benefit is explicitly disabled by penalty records.
     */
    @Override
    public boolean isBenefitExplicitlyDisabled(String userId, String benefitCode) {
        if (!StringUtils.hasText(benefitCode)) {
            return false;
        }
        return loadBenefitState(userId).disabledCodes.contains(benefitCode);
    }

    /**
     * Resolves current topic quota for the target user.
     */
    @Override
    public int resolveTopicQuota(String userId) {
        ContentUserProfile profile = profileMapper == null ? null : profileMapper.selectByUserId(userId);
        return resolveTopicQuota(resolveRuntimeBenefit(profile), loadBenefitState(userId));
    }

    /**
     * 输出推荐系统可用的等级加权，质量分缺失时显式失败。
     */
    @Override
    public ContentUserDistributionWeightVO resolveDistributionWeight(String userId, BigDecimal qualityScore) {
        if (qualityScore == null) {
            throw new JeecgBootException("推荐加权必须提供内容质量分");
        }
        ContentUserProfile profile = profileMapper == null ? null : profileMapper.selectByUserId(userId);
        RuntimeBenefit runtimeBenefit = resolveRuntimeBenefit(profile);
        BigDecimal weight = runtimeBenefit.distributionWeight == null
            ? DEFAULT_DISTRIBUTION_WEIGHT
            : runtimeBenefit.distributionWeight.min(MAX_DISTRIBUTION_WEIGHT);
        if (defaultLevel(profile) < 2) {
            weight = DEFAULT_DISTRIBUTION_WEIGHT;
        }
        return new ContentUserDistributionWeightVO()
            .setUserId(userId)
            .setLevel(defaultLevel(profile))
            .setQualityScore(qualityScore)
            .setQualityScoreRequired(Boolean.TRUE)
            .setDistributionWeight(weight.setScale(2, RoundingMode.HALF_UP))
            .setWeighted(weight.compareTo(DEFAULT_DISTRIBUTION_WEIGHT) > 0);
    }

    private BenefitState loadBenefitState(String userId) {
        BenefitState state = new BenefitState();
        if (!StringUtils.hasText(userId) || levelBenefitPenaltyRecordMapper == null) {
            return state;
        }
        List<ContentUserLevelBenefitPenaltyRecord> records = levelBenefitPenaltyRecordMapper.selectList(
            Wrappers.<ContentUserLevelBenefitPenaltyRecord>lambdaQuery()
                .eq(ContentUserLevelBenefitPenaltyRecord::getUserId, userId)
        );
        if (records == null || records.isEmpty()) {
            return state;
        }
        records.stream()
            .filter(item -> StringUtils.hasText(item.getBenefitCode()))
            .sorted(Comparator.comparing(this::resolveRecordSortTime))
            .forEach(item -> applyBenefitRecord(state, item));
        return state;
    }

    private void applyBenefitRecord(BenefitState state, ContentUserLevelBenefitPenaltyRecord item) {
        if (STATUS_PENDING_RECOVER.equals(item.getRecoverStatus()) && Boolean.FALSE.equals(item.getCurrentEnabled())) {
            state.disabledCodes.add(item.getBenefitCode());
            state.enabledCodes.remove(item.getBenefitCode());
            return;
        }
        if (STATUS_RECOVERED.equals(item.getRecoverStatus()) && Boolean.TRUE.equals(item.getCurrentEnabled())) {
            state.disabledCodes.remove(item.getBenefitCode());
            state.enabledCodes.add(item.getBenefitCode());
        }
    }

    private java.util.Date resolveRecordSortTime(ContentUserLevelBenefitPenaltyRecord item) {
        if (item.getRecoveredAt() != null) {
            return item.getRecoveredAt();
        }
        if (item.getUpdateTime() != null) {
            return item.getUpdateTime();
        }
        if (item.getCreateTime() != null) {
            return item.getCreateTime();
        }
        return new java.util.Date(0L);
    }

    private boolean isHighLevelUser(ContentUserProfile profile) {
        if (profile == null) {
            return false;
        }
        int level = profile.getLevel() == null ? 1 : profile.getLevel();
        int growthValue = profile.getGrowthValue() == null ? 0 : profile.getGrowthValue();
        return level >= 5 || growthValue >= 400;
    }

    private int resolveUploadLimit(RuntimeBenefit runtimeBenefit, BenefitState state) {
        if (state.disabledCodes.contains(BENEFIT_UPLOAD_EXPANDED)) {
            return DEFAULT_UPLOAD_LIMIT_MB;
        }
        if (state.enabledCodes.contains(BENEFIT_UPLOAD_EXPANDED)) {
            return ENHANCED_UPLOAD_LIMIT_MB;
        }
        return runtimeBenefit.uploadSizeLimitMb;
    }

    private boolean resolveHdVideoEnabled(RuntimeBenefit runtimeBenefit, BenefitState state) {
        if (state.disabledCodes.contains(BENEFIT_HD_VIDEO)) {
            return false;
        }
        return state.enabledCodes.contains(BENEFIT_HD_VIDEO) || runtimeBenefit.hdVideoEnabled;
    }

    private int resolveTopicQuota(RuntimeBenefit runtimeBenefit, BenefitState state) {
        if (state.disabledCodes.contains(BENEFIT_TOPIC_QUOTA_EXPANDED)) {
            return DEFAULT_TOPIC_QUOTA;
        }
        if (state.enabledCodes.contains(BENEFIT_TOPIC_QUOTA_EXPANDED)) {
            return ENHANCED_TOPIC_QUOTA;
        }
        return runtimeBenefit.topicQuota;
    }

    private int resolveSupportPriority(RuntimeBenefit runtimeBenefit) {
        return runtimeBenefit.supportPriority == null ? DEFAULT_SUPPORT_PRIORITY : runtimeBenefit.supportPriority;
    }

    private RuntimeBenefit resolveRuntimeBenefit(ContentUserProfile profile) {
        RuntimeBenefit runtimeBenefit = RuntimeBenefit.defaults(isHighLevelUser(profile));
        List<ContentUserLevelBenefitConfig> configs = loadBenefitConfigs(defaultLevel(profile));
        for (ContentUserLevelBenefitConfig config : configs) {
            applyConfig(runtimeBenefit, config);
        }
        return runtimeBenefit;
    }

    private List<ContentUserLevelBenefitConfig> loadBenefitConfigs(int level) {
        if (levelBenefitConfigMapper == null) {
            return List.of();
        }
        Wrapper<ContentUserLevelBenefitConfig> wrapper = Wrappers.<ContentUserLevelBenefitConfig>lambdaQuery()
            .eq(ContentUserLevelBenefitConfig::getEnabled, Boolean.TRUE)
            .le(ContentUserLevelBenefitConfig::getLevel, level)
            .orderByAsc(ContentUserLevelBenefitConfig::getLevel);
        List<ContentUserLevelBenefitConfig> configs = levelBenefitConfigMapper.selectList(wrapper);
        if (configs == null) {
            return List.of();
        }
        return configs.stream()
            .filter(item -> Boolean.TRUE.equals(item.getEnabled()) && item.getLevel() != null && item.getLevel() <= level)
            .sorted(Comparator.comparing(ContentUserLevelBenefitConfig::getLevel))
            .toList();
    }

    private void applyConfig(RuntimeBenefit runtimeBenefit, ContentUserLevelBenefitConfig config) {
        if (!isSupportedConfig(config)) {
            return;
        }
        if (!isBlank(config.getBenefitConfigJson()) && !looksLikeJsonObject(config.getBenefitConfigJson())) {
            return;
        }
        String key = config.getBenefitKey();
        String value = config.getBenefitValue();
        try {
            if (BENEFIT_UPLOAD_LIMIT_MB.equals(key)) {
                int uploadLimit = Integer.parseInt(value);
                if (uploadLimit >= DEFAULT_UPLOAD_LIMIT_MB) {
                    runtimeBenefit.uploadSizeLimitMb = uploadLimit;
                    runtimeBenefit.enabledConfigKeys.add(key);
                }
            } else if (BENEFIT_UPLOAD_EXPANDED.equals(key)) {
                runtimeBenefit.uploadSizeLimitMb = ENHANCED_UPLOAD_LIMIT_MB;
                runtimeBenefit.enabledConfigKeys.add(key);
            } else if (BENEFIT_HD_VIDEO.equals(key)) {
                runtimeBenefit.hdVideoEnabled = Boolean.parseBoolean(value);
                if (runtimeBenefit.hdVideoEnabled) {
                    runtimeBenefit.enabledConfigKeys.add(key);
                }
            } else if (BENEFIT_TOPIC_QUOTA.equals(key)) {
                int topicQuota = Integer.parseInt(value);
                if (topicQuota >= 0) {
                    runtimeBenefit.topicQuota = topicQuota;
                    runtimeBenefit.enabledConfigKeys.add(key);
                }
            } else if (BENEFIT_TOPIC_QUOTA_EXPANDED.equals(key)) {
                runtimeBenefit.topicQuota = ENHANCED_TOPIC_QUOTA;
                runtimeBenefit.enabledConfigKeys.add(key);
            } else if (BENEFIT_SUPPORT_PRIORITY.equals(key)) {
                int supportPriority = Integer.parseInt(value);
                if (supportPriority >= 0) {
                    runtimeBenefit.supportPriority = supportPriority;
                    runtimeBenefit.enabledConfigKeys.add(key);
                }
            } else if (BENEFIT_LEVEL_BADGE_STYLE.equals(key) && value.length() <= 64) {
                runtimeBenefit.levelBadgeStyleKey = value;
                runtimeBenefit.enabledConfigKeys.add(key);
            } else if (BENEFIT_COMMENT_EFFECT.equals(key) && value.length() <= 64) {
                runtimeBenefit.commentEffectKey = value;
                runtimeBenefit.enabledConfigKeys.add(key);
            } else if (BENEFIT_DISTRIBUTION_WEIGHT.equals(key)) {
                BigDecimal weight = new BigDecimal(value);
                if (weight.compareTo(DEFAULT_DISTRIBUTION_WEIGHT) >= 0) {
                    runtimeBenefit.distributionWeight = weight.min(MAX_DISTRIBUTION_WEIGHT);
                    runtimeBenefit.enabledConfigKeys.add(key);
                }
            }
        } catch (NumberFormatException ex) {
            // 坏配置不参与运行时权益，避免影响上一条有效配置。
        }
    }

    private boolean isSupportedConfig(ContentUserLevelBenefitConfig config) {
        return config != null
            && config.getLevel() != null
            && config.getLevel() >= 1
            && StringUtils.hasText(config.getBenefitKey())
            && SUPPORTED_CONFIG_KEYS.contains(config.getBenefitKey())
            && !isBlank(config.getBenefitValue());
    }

    private boolean looksLikeJsonObject(String value) {
        String trimmed = value.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int defaultLevel(ContentUserProfile profile) {
        return profile == null || profile.getLevel() == null ? 1 : Math.max(1, profile.getLevel());
    }

    private static final class BenefitState {
        private final Set<String> enabledCodes = new LinkedHashSet<>();
        private final Set<String> disabledCodes = new LinkedHashSet<>();
    }

    private static final class RuntimeBenefit {
        private int uploadSizeLimitMb;
        private boolean hdVideoEnabled;
        private int topicQuota;
        private Integer supportPriority;
        private String levelBadgeStyleKey;
        private String commentEffectKey;
        private BigDecimal distributionWeight;
        private final Set<String> enabledConfigKeys = new HashSet<>();

        private static RuntimeBenefit defaults(boolean highLevelUser) {
            RuntimeBenefit benefit = new RuntimeBenefit();
            benefit.uploadSizeLimitMb = highLevelUser ? ENHANCED_UPLOAD_LIMIT_MB : DEFAULT_UPLOAD_LIMIT_MB;
            benefit.hdVideoEnabled = highLevelUser;
            benefit.topicQuota = highLevelUser ? ENHANCED_TOPIC_QUOTA : DEFAULT_TOPIC_QUOTA;
            benefit.supportPriority = highLevelUser ? ENHANCED_SUPPORT_PRIORITY : DEFAULT_SUPPORT_PRIORITY;
            benefit.distributionWeight = DEFAULT_DISTRIBUTION_WEIGHT;
            return benefit;
        }
    }
}
