package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserGrowthPenaltyRecord;

import java.util.Date;

/**
 * Service contract for level benefit recovery orchestration.
 */
public interface IContentUserLevelBenefitRecoveryService {

    /**
     * Restores level benefits linked to the specified growth penalty record.
     */
    int recoverByPenaltyRecord(ContentUserGrowthPenaltyRecord record,
                               String operatorUserId,
                               Date executeTime,
                               String reason);

    /**
     * Checks whether the specified benefit is explicitly enabled for the user.
     */
    boolean hasEnabledBenefit(String userId, String benefitCode);
}
