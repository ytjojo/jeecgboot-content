package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.constant.ContentUserRewardSourceTypeConstant;
import org.jeecg.modules.content.user.dto.ContentUserRewardEventDTO;
import org.jeecg.modules.content.user.dto.ContentUserRewardResultDTO;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserGrowthLedger;
import org.jeecg.modules.content.user.entity.ContentUserPointLedger;
import org.jeecg.modules.content.user.entity.ContentUserRewardEvent;
import org.jeecg.modules.content.user.entity.ContentUserRewardRule;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserPointLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRewardEventMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserGrowthServiceImpl;
import org.jeecg.modules.content.user.vo.ContentUserLevelBenefitSummaryVO;
import org.jeecg.modules.content.user.vo.ContentUserGrowthVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
    private ContentUserRewardEventMapper rewardEventMapper;

    @Mock
    private IContentUserRewardRuleService rewardRuleService;

    @Mock
    private IContentUserLevelBenefitService levelBenefitService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ContentUserGrowthServiceImpl growthService;

    @Test
    void shouldKeepPointsAndGrowthInSeparateLedgers() {
        growthService.recordBehavior("u1", "CONTENT_PUBLISH", 20, 15);

        verify(pointLedgerMapper).insert(argThat((ContentUserPointLedger it) -> it.getPointDelta() == 20));
        verify(growthLedgerMapper).insert(argThat((ContentUserGrowthLedger it) -> it.getGrowthDelta() == 15));
    }

    @Test
    void shouldAwardConfiguredRewardAndWriteAuditableLedgers() {
        when(rewardEventMapper.selectOne(any())).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), eq("1"), eq(10L), eq(TimeUnit.MINUTES))).thenReturn(Boolean.TRUE);
        when(valueOperations.increment(any(), eq(10L))).thenReturn(10L);
        when(valueOperations.increment(any(), eq(8L))).thenReturn(8L);
        when(rewardRuleService.getEnabledRule(ContentUserRewardSourceTypeConstant.CONTENT_PUBLISH)).thenReturn(Optional.of(rule("R_CONTENT", 10, 8, 100, 80)));
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setPointBalance(20)
            .setGrowthValue(90));

        ContentUserRewardResultDTO result = growthService.reward(new ContentUserRewardEventDTO()
            .setUserId("u1")
            .setSourceType(ContentUserRewardSourceTypeConstant.CONTENT_PUBLISH)
            .setEventId("evt-1")
            .setBizId("post-1")
            .setDailyBucket("20260518"));

        assertThat(result.getPointDelta()).isEqualTo(10);
        assertThat(result.getGrowthDelta()).isEqualTo(8);
        assertThat(result.getRuleCode()).isEqualTo("R_CONTENT");
        verify(pointLedgerMapper).insert(argThat((ContentUserPointLedger it) ->
            "evt-1".equals(it.getEventId()) && it.getBalanceAfter() == 30 && "20260518".equals(it.getDailyBucket())));
        verify(growthLedgerMapper).insert(argThat((ContentUserGrowthLedger it) ->
            "evt-1".equals(it.getEventId()) && it.getGrowthAfter() == 98));
        verify(rewardEventMapper).insert(argThat((ContentUserRewardEvent it) ->
            "SUCCESS".equals(it.getProcessStatus()) && it.getPointDelta() == 10 && it.getGrowthDelta() == 8));
    }

    @Test
    void shouldReturnExistingResultForDuplicateEventWithoutAwardingAgain() {
        when(rewardEventMapper.selectOne(any())).thenReturn(new ContentUserRewardEvent()
            .setEventId("evt-dup")
            .setUserId("u1")
            .setSourceType(ContentUserRewardSourceTypeConstant.LIKE)
            .setRuleCode("R_LIKE")
            .setPointDelta(1)
            .setGrowthDelta(1)
            .setProcessStatus("SUCCESS"));

        ContentUserRewardResultDTO result = growthService.reward(new ContentUserRewardEventDTO()
            .setUserId("u1")
            .setSourceType(ContentUserRewardSourceTypeConstant.LIKE)
            .setEventId("evt-dup"));

        assertThat(result.getDuplicate()).isTrue();
        verify(pointLedgerMapper, never()).insert(any(ContentUserPointLedger.class));
        verify(growthLedgerMapper, never()).insert(any(ContentUserGrowthLedger.class));
        verify(rewardEventMapper, never()).insert(argThat((ContentUserRewardEvent it) -> true));
    }

    @Test
    void shouldSkipRewardWhenDailyCapIsReached() {
        when(rewardEventMapper.selectOne(any())).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), eq("1"), eq(10L), eq(TimeUnit.MINUTES))).thenReturn(Boolean.TRUE);
        when(valueOperations.increment(any(), eq(2L))).thenReturn(6L);
        when(rewardRuleService.getEnabledRule(ContentUserRewardSourceTypeConstant.SHARE)).thenReturn(Optional.of(rule("R_SHARE", 2, 0, 5, null)));

        ContentUserRewardResultDTO result = growthService.reward(new ContentUserRewardEventDTO()
            .setUserId("u1")
            .setSourceType(ContentUserRewardSourceTypeConstant.SHARE)
            .setEventId("evt-cap")
            .setDailyBucket("20260518"));

        assertThat(result.getProcessStatus()).isEqualTo("SKIPPED");
        assertThat(result.getSkipReason()).isEqualTo("DAILY_CAP_REACHED");
        verify(pointLedgerMapper, never()).insert(any(ContentUserPointLedger.class));
        verify(rewardEventMapper).insert(argThat((ContentUserRewardEvent it) ->
            "SKIPPED".equals(it.getProcessStatus()) && it.getPointDelta() == 0));
    }

    @Test
    void shouldSkipRewardWhenRuleIsDisabledOrMissing() {
        when(rewardEventMapper.selectOne(any())).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), eq("1"), eq(10L), eq(TimeUnit.MINUTES))).thenReturn(Boolean.TRUE);
        when(rewardRuleService.getEnabledRule(ContentUserRewardSourceTypeConstant.COMMENT)).thenReturn(Optional.empty());

        ContentUserRewardResultDTO result = growthService.reward(new ContentUserRewardEventDTO()
            .setUserId("u1")
            .setSourceType(ContentUserRewardSourceTypeConstant.COMMENT)
            .setEventId("evt-no-rule"));

        assertThat(result.getProcessStatus()).isEqualTo("SKIPPED");
        assertThat(result.getSkipReason()).isEqualTo("NO_ENABLED_RULE");
        verify(pointLedgerMapper, never()).insert(any(ContentUserPointLedger.class));
    }

    @Test
    void shouldRejectUnsupportedSourceAndIllegalAmounts() {
        assertThatThrownBy(() -> growthService.reward(new ContentUserRewardEventDTO()
            .setUserId("u1")
            .setSourceType("BAD")
            .setEventId("evt-bad")))
            .isInstanceOf(JeecgBootException.class);

        assertThatThrownBy(() -> growthService.reward(new ContentUserRewardEventDTO()
            .setUserId("u1")
            .setSourceType(ContentUserRewardSourceTypeConstant.LIKE)
            .setEventId("evt-bad-amount")
            .setPointAmount(-1)))
            .isInstanceOf(JeecgBootException.class);
    }

    @Test
    void shouldCoverDailyCapsForSocialBehaviors() {
        assertThat(ContentUserRewardSourceTypeConstant.SUPPORTED_TYPES).contains(
            ContentUserRewardSourceTypeConstant.LIKE,
            ContentUserRewardSourceTypeConstant.SHARE,
            ContentUserRewardSourceTypeConstant.COMMENT,
            ContentUserRewardSourceTypeConstant.REPOST,
            ContentUserRewardSourceTypeConstant.FOLLOWED
        );
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

    private ContentUserRewardRule rule(String ruleCode, int pointAmount, int growthAmount, Integer pointCap, Integer growthCap) {
        return new ContentUserRewardRule()
            .setRuleCode(ruleCode)
            .setSourceType(ContentUserRewardSourceTypeConstant.CONTENT_PUBLISH)
            .setPointAmount(pointAmount)
            .setGrowthAmount(growthAmount)
            .setDailyPointCap(pointCap)
            .setDailyGrowthCap(growthCap)
            .setRuleDescription("奖励规则")
            .setEnabled(Boolean.TRUE);
    }
}
