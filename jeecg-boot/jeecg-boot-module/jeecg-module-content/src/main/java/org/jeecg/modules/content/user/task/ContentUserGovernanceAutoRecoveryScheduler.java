package org.jeecg.modules.content.user.task;

import jakarta.annotation.Resource;
import org.jeecg.modules.content.user.service.IContentUserGovernanceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Scheduler for recovering expired governance statuses.
 */
@Component
public class ContentUserGovernanceAutoRecoveryScheduler {

    @Resource
    private IContentUserGovernanceService governanceService;

    /**
     * Periodically restores expired recoverable governance statuses.
     */
    @Scheduled(fixedDelayString = "${content.user.governance.auto-recover.fixed-delay-ms:60000}")
    public void autoRecoverExpiredStatuses() {
        governanceService.autoRecoverExpiredStatuses(new Date(), 100L);
    }
}
