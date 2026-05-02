package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.vo.ContentUserLevelBenefitSummaryVO;

/**
 * Service contract for runtime level benefit evaluation.
 */
public interface IContentUserLevelBenefitService {

    ContentUserLevelBenefitSummaryVO getBenefitSummary(String userId);

    boolean hasEnabledBenefit(String userId, String benefitCode);

    boolean isBenefitExplicitlyDisabled(String userId, String benefitCode);

    int resolveTopicQuota(String userId);
}
