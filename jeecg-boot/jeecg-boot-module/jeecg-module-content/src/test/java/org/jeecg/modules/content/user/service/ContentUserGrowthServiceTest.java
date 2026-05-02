package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserGrowthLedger;
import org.jeecg.modules.content.user.entity.ContentUserPointLedger;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserPointLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserGrowthServiceImpl;
import org.jeecg.modules.content.user.vo.ContentUserLevelBenefitSummaryVO;
import org.jeecg.modules.content.user.vo.ContentUserGrowthVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentUserGrowthServiceTest {

    @Mock
    private ContentUserPointLedgerMapper pointLedgerMapper;

    @Mock
    private ContentUserGrowthLedgerMapper growthLedgerMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private IContentUserLevelBenefitService levelBenefitService;

    @InjectMocks
    private ContentUserGrowthServiceImpl growthService;

    @Test
    void shouldKeepPointsAndGrowthInSeparateLedgers() {
        growthService.recordBehavior("u1", "CONTENT_PUBLISH", 20, 15);

        verify(pointLedgerMapper).insert(argThat((ContentUserPointLedger it) -> it.getPointDelta() == 20));
        verify(growthLedgerMapper).insert(argThat((ContentUserGrowthLedger it) -> it.getGrowthDelta() == 15));
    }

    @Test
    void shouldReturnGrowthSummaryWithBenefitSummary() {
        when(profileMapper.selectOne(any())).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setPointBalance(20)
            .setGrowthValue(430)
            .setLevel(5));
        when(levelBenefitService.getBenefitSummary("u1")).thenReturn(new ContentUserLevelBenefitSummaryVO()
            .setUploadSizeLimitMb(500)
            .setHdVideoEnabled(Boolean.TRUE)
            .setTopicQuota(30)
            .setEnabledBenefitCodes(List.of("HD_VIDEO")));

        ContentUserGrowthVO result = growthService.getGrowthSummary("u1");

        assertThat(result.getLevelBenefitSummary()).isNotNull();
        assertThat(result.getLevelBenefitSummary().getUploadSizeLimitMb()).isEqualTo(500);
        assertThat(result.getLevelBenefitSummary().getHdVideoEnabled()).isTrue();
    }
}
