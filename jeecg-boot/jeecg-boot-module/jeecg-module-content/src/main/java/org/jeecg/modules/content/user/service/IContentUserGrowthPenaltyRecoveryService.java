package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserAppeal;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;

import java.util.Date;

/**
 * Service contract for growth penalty recovery orchestration.
 */
public interface IContentUserGrowthPenaltyRecoveryService {

    int recoverByAppeal(ContentUserAppeal appeal, String operatorUserId, Date executeTime, String reason);

    int recoverByGovernanceRecord(ContentUserStatusRecord record, String operatorUserId, Date executeTime, String reason);
}
