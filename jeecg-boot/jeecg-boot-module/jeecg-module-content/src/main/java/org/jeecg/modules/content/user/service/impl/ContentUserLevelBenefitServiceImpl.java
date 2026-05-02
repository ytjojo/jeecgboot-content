package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.jeecg.modules.content.user.entity.ContentUserLevelBenefitPenaltyRecord;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.mapper.ContentUserLevelBenefitPenaltyRecordMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitService;
import org.jeecg.modules.content.user.vo.ContentUserLevelBenefitSummaryVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
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

    private static final String BENEFIT_UPLOAD_EXPANDED = "UPLOAD_EXPANDED";
    private static final String BENEFIT_HD_VIDEO = "HD_VIDEO";
    private static final String BENEFIT_TOPIC_QUOTA_EXPANDED = "TOPIC_QUOTA_EXPANDED";
    private static final String STATUS_PENDING_RECOVER = "PENDING_RECOVER";
    private static final String STATUS_RECOVERED = "RECOVERED";

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserLevelBenefitPenaltyRecordMapper levelBenefitPenaltyRecordMapper;

    /**
     * Builds runtime benefit summary for the target user.
     */
    @Override
    public ContentUserLevelBenefitSummaryVO getBenefitSummary(String userId) {
        ContentUserProfile profile = profileMapper == null ? null : profileMapper.selectByUserId(userId);
        BenefitState state = loadBenefitState(userId);
        boolean highLevelUser = isHighLevelUser(profile);
        return new ContentUserLevelBenefitSummaryVO()
            .setUploadSizeLimitMb(resolveUploadLimit(highLevelUser, state))
            .setHdVideoEnabled(resolveHdVideoEnabled(highLevelUser, state))
            .setTopicQuota(resolveTopicQuota(highLevelUser, state))
            .setEnabledBenefitCodes(new ArrayList<>(state.enabledCodes));
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
        boolean highLevelUser = isHighLevelUser(profile);
        if (BENEFIT_UPLOAD_EXPANDED.equals(benefitCode)) {
            return highLevelUser;
        }
        if (BENEFIT_HD_VIDEO.equals(benefitCode)) {
            return highLevelUser;
        }
        if (BENEFIT_TOPIC_QUOTA_EXPANDED.equals(benefitCode)) {
            return highLevelUser;
        }
        return false;
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
        return resolveTopicQuota(isHighLevelUser(profile), loadBenefitState(userId));
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

    private int resolveUploadLimit(boolean highLevelUser, BenefitState state) {
        if (state.disabledCodes.contains(BENEFIT_UPLOAD_EXPANDED)) {
            return DEFAULT_UPLOAD_LIMIT_MB;
        }
        if (state.enabledCodes.contains(BENEFIT_UPLOAD_EXPANDED) || highLevelUser) {
            return ENHANCED_UPLOAD_LIMIT_MB;
        }
        return DEFAULT_UPLOAD_LIMIT_MB;
    }

    private boolean resolveHdVideoEnabled(boolean highLevelUser, BenefitState state) {
        if (state.disabledCodes.contains(BENEFIT_HD_VIDEO)) {
            return false;
        }
        return state.enabledCodes.contains(BENEFIT_HD_VIDEO) || highLevelUser;
    }

    private int resolveTopicQuota(boolean highLevelUser, BenefitState state) {
        if (state.disabledCodes.contains(BENEFIT_TOPIC_QUOTA_EXPANDED)) {
            return DEFAULT_TOPIC_QUOTA;
        }
        if (state.enabledCodes.contains(BENEFIT_TOPIC_QUOTA_EXPANDED) || highLevelUser) {
            return ENHANCED_TOPIC_QUOTA;
        }
        return DEFAULT_TOPIC_QUOTA;
    }

    private static final class BenefitState {
        private final Set<String> enabledCodes = new LinkedHashSet<>();
        private final Set<String> disabledCodes = new LinkedHashSet<>();
    }
}
