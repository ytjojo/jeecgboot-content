package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.vo.ContentUserLevelBenefitSummaryVO;
import org.jeecg.modules.content.user.vo.ContentUserDistributionWeightVO;

import java.math.BigDecimal;

/**
 * Service contract for runtime level benefit evaluation.
 */
public interface IContentUserLevelBenefitService {

    ContentUserLevelBenefitSummaryVO getBenefitSummary(String userId);

    boolean hasEnabledBenefit(String userId, String benefitCode);

    boolean isBenefitExplicitlyDisabled(String userId, String benefitCode);

    int resolveTopicQuota(String userId);

    /**
     * 解析推荐系统可使用的等级加权信号。
     */
    ContentUserDistributionWeightVO resolveDistributionWeight(String userId, BigDecimal qualityScore);
}
