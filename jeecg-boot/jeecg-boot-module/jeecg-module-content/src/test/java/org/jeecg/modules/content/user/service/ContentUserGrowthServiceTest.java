package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserGrowthLedger;
import org.jeecg.modules.content.user.entity.ContentUserPointLedger;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserPointLedgerMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserGrowthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContentUserGrowthServiceTest {

    @Mock
    private ContentUserPointLedgerMapper pointLedgerMapper;

    @Mock
    private ContentUserGrowthLedgerMapper growthLedgerMapper;

    @InjectMocks
    private ContentUserGrowthServiceImpl growthService;

    @Test
    void shouldKeepPointsAndGrowthInSeparateLedgers() {
        growthService.recordBehavior("u1", "CONTENT_PUBLISH", 20, 15);

        verify(pointLedgerMapper).insert(argThat((ContentUserPointLedger it) -> it.getPointDelta() == 20));
        verify(growthLedgerMapper).insert(argThat((ContentUserGrowthLedger it) -> it.getGrowthDelta() == 15));
    }
}
