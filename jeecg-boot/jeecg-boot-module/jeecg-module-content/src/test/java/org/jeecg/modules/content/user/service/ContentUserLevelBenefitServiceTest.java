package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserLevelBenefitPenaltyRecord;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.mapper.ContentUserLevelBenefitPenaltyRecordMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserLevelBenefitServiceImpl;
import org.jeecg.modules.content.user.vo.ContentUserLevelBenefitSummaryVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests for content user level benefit service.
 */
@ExtendWith(MockitoExtension.class)
class ContentUserLevelBenefitServiceTest {

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private ContentUserLevelBenefitPenaltyRecordMapper levelBenefitPenaltyRecordMapper;

    @InjectMocks
    private ContentUserLevelBenefitServiceImpl levelBenefitService;

    @Test
    void shouldReturnDefaultBenefitSummaryForRegularUser() {
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setLevel(2)
            .setGrowthValue(150)
            .setStatus("NORMAL"));
        when(levelBenefitPenaltyRecordMapper.selectList(any())).thenReturn(List.of());

        ContentUserLevelBenefitSummaryVO result = levelBenefitService.getBenefitSummary("u1");

        assertThat(result.getUploadSizeLimitMb()).isEqualTo(100);
        assertThat(result.getHdVideoEnabled()).isFalse();
        assertThat(result.getTopicQuota()).isEqualTo(10);
        assertThat(result.getEnabledBenefitCodes()).isEmpty();
    }

    @Test
    void shouldReturnEnhancedBenefitSummaryForHighLevelUser() {
        when(profileMapper.selectByUserId("u2")).thenReturn(new ContentUserProfile()
            .setUserId("u2")
            .setLevel(5)
            .setGrowthValue(420)
            .setStatus("NORMAL"));
        when(levelBenefitPenaltyRecordMapper.selectList(any())).thenReturn(List.of());

        ContentUserLevelBenefitSummaryVO result = levelBenefitService.getBenefitSummary("u2");

        assertThat(result.getUploadSizeLimitMb()).isEqualTo(500);
        assertThat(result.getHdVideoEnabled()).isTrue();
        assertThat(result.getTopicQuota()).isEqualTo(30);
    }

    @Test
    void shouldEnableBenefitByRecoveredExplicitRecord() {
        when(profileMapper.selectByUserId("u3")).thenReturn(new ContentUserProfile()
            .setUserId("u3")
            .setLevel(1)
            .setGrowthValue(0)
            .setStatus("NORMAL"));
        when(levelBenefitPenaltyRecordMapper.selectList(any())).thenReturn(List.of(
            new ContentUserLevelBenefitPenaltyRecord()
                .setUserId("u3")
                .setBenefitCode("HD_VIDEO")
                .setCurrentEnabled(Boolean.TRUE)
                .setRecoverStatus("RECOVERED")
        ));

        ContentUserLevelBenefitSummaryVO result = levelBenefitService.getBenefitSummary("u3");

        assertThat(result.getHdVideoEnabled()).isTrue();
        assertThat(result.getEnabledBenefitCodes()).contains("HD_VIDEO");
    }

    @Test
    void shouldPreferExplicitDisableOverHighLevelRule() {
        when(profileMapper.selectByUserId("u4")).thenReturn(new ContentUserProfile()
            .setUserId("u4")
            .setLevel(6)
            .setGrowthValue(600)
            .setStatus("NORMAL"));
        when(levelBenefitPenaltyRecordMapper.selectList(any())).thenReturn(List.of(
            new ContentUserLevelBenefitPenaltyRecord()
                .setUserId("u4")
                .setBenefitCode("TOPIC_QUOTA_EXPANDED")
                .setCurrentEnabled(Boolean.FALSE)
                .setRecoverStatus("PENDING_RECOVER")
        ));

        ContentUserLevelBenefitSummaryVO result = levelBenefitService.getBenefitSummary("u4");

        assertThat(result.getTopicQuota()).isEqualTo(10);
        assertThat(levelBenefitService.hasEnabledBenefit("u4", "TOPIC_QUOTA_EXPANDED")).isFalse();
    }

    @Test
    void shouldUseLatestRecordStateForSameBenefitCode() {
        when(profileMapper.selectByUserId("u5")).thenReturn(new ContentUserProfile()
            .setUserId("u5")
            .setLevel(1)
            .setGrowthValue(0)
            .setStatus("NORMAL"));
        when(levelBenefitPenaltyRecordMapper.selectList(any())).thenReturn(List.of(
            new ContentUserLevelBenefitPenaltyRecord()
                .setUserId("u5")
                .setBenefitCode("HD_VIDEO")
                .setCurrentEnabled(Boolean.FALSE)
                .setRecoverStatus("PENDING_RECOVER")
                .setRecoveredAt(new Date(1000L)),
            new ContentUserLevelBenefitPenaltyRecord()
                .setUserId("u5")
                .setBenefitCode("HD_VIDEO")
                .setCurrentEnabled(Boolean.TRUE)
                .setRecoverStatus("RECOVERED")
                .setRecoveredAt(new Date(2000L))
        ));

        ContentUserLevelBenefitSummaryVO result = levelBenefitService.getBenefitSummary("u5");

        assertThat(result.getHdVideoEnabled()).isTrue();
        assertThat(levelBenefitService.hasEnabledBenefit("u5", "HD_VIDEO")).isTrue();
        assertThat(levelBenefitService.isBenefitExplicitlyDisabled("u5", "HD_VIDEO")).isFalse();
    }
}
