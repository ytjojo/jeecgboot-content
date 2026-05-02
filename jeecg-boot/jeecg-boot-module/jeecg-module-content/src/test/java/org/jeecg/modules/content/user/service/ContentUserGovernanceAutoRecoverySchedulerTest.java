package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.task.ContentUserGovernanceAutoRecoveryScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the governance auto recovery scheduler.
 */
@ExtendWith(MockitoExtension.class)
class ContentUserGovernanceAutoRecoverySchedulerTest {

    @Mock
    private IContentUserGovernanceService governanceService;

    @InjectMocks
    private ContentUserGovernanceAutoRecoveryScheduler scheduler;

    @Test
    void shouldDelegateExpiredStatusRecoveryToGovernanceService() {
        scheduler.autoRecoverExpiredStatuses();

        verify(governanceService).autoRecoverExpiredStatuses(any(Date.class), eq(100L));
    }
}
