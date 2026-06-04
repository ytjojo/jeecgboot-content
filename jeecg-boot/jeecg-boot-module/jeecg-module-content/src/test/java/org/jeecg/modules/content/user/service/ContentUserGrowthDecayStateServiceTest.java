package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserGrowthDecayState;
import org.jeecg.modules.content.user.entity.ContentUserGrowthLedger;
import org.jeecg.modules.content.user.entity.ContentUserLevelConfig;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthDecayStateMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserGrowthDecayStateServiceImpl;
import org.jeecg.modules.content.user.vo.ContentUserGrowthDecayRuleVO;
import org.jeecg.modules.content.user.vo.ContentUserGrowthDecayStatusVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 内容社区成长值衰减状态服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserGrowthDecayStateServiceTest {

    @Mock
    private ContentUserGrowthDecayStateMapper decayStateMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private ContentUserGrowthLedgerMapper growthLedgerMapper;

    @Mock
    private ContentUserDeviceSessionMapper deviceSessionMapper;

    @Mock
    private ContentUserAuditLogMapper auditLogMapper;

    @Mock
    private IContentUserLevelConfigService levelConfigService;

    private ContentUserGrowthDecayStateServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ContentUserGrowthDecayStateServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", decayStateMapper);
        ReflectionTestUtils.setField(service, "profileMapper", profileMapper);
        ReflectionTestUtils.setField(service, "growthLedgerMapper", growthLedgerMapper);
        ReflectionTestUtils.setField(service, "deviceSessionMapper", deviceSessionMapper);
        ReflectionTestUtils.setField(service, "auditLogMapper", auditLogMapper);
        ReflectionTestUtils.setField(service, "levelConfigService", levelConfigService);
        lenient().when(levelConfigService.calculateLevel(any())).thenAnswer(invocation -> {
            Integer growthValue = invocation.getArgument(0, Integer.class);
            int safeGrowth = Math.max(growthValue == null ? 0 : growthValue, 0);
            return Math.max(1, safeGrowth / 100 + 1);
        });
        lenient().when(levelConfigService.listValidEnabledLevels()).thenReturn(List.of(
            level(1, 0),
            level(2, 100),
            level(3, 200)
        ));
    }

    @Test
    void shouldSelectOnlyDay31InactiveUsersAsDecayCandidates() {
        Date runTime = time(2026, 5, 19, 0, 0);
        ContentUserProfile day30User = new ContentUserProfile().setUserId("u30").setGrowthValue(100);
        ContentUserProfile day31User = new ContentUserProfile().setUserId("u31").setGrowthValue(100);
        when(profileMapper.selectList(any())).thenReturn(List.of(day30User, day31User));
        when(deviceSessionMapper.selectLatestActiveTimeByUserId("u30")).thenReturn(time(2026, 4, 19, 0, 0));
        when(deviceSessionMapper.selectLatestActiveTimeByUserId("u31")).thenReturn(time(2026, 4, 18, 23, 59));

        List<ContentUserProfile> result = service.listDecayCandidates(runTime);

        assertThat(result).extracting(ContentUserProfile::getUserId).containsExactly("u31");
    }

    @Test
    void shouldWriteNegativeGrowthLedgerAndDecayStateWithConfiguredRate() {
        Date runTime = time(2026, 5, 19, 10, 0);
        when(decayStateMapper.selectList(any())).thenReturn(List.of());
        when(profileMapper.selectList(any())).thenReturn(List.of(new ContentUserProfile()
            .setUserId("u1")
            .setGrowthValue(200)
            .setLevel(2)));
        when(deviceSessionMapper.selectLatestActiveTimeByUserId("u1")).thenReturn(time(2026, 4, 1, 0, 0));
        when(decayStateMapper.selectOne(any())).thenReturn(null);

        int affected = service.executeDecay(runTime, rule("0.10", true));

        assertThat(affected).isEqualTo(1);
        verify(growthLedgerMapper).insert(argThat((ContentUserGrowthLedger it) ->
            "u1".equals(it.getUserId())
                && "GROWTH_DECAY".equals(it.getSourceType())
                && Integer.valueOf(-20).equals(it.getGrowthDelta())
                && Integer.valueOf(180).equals(it.getGrowthAfter())));
        verify(profileMapper).updateById(argThat((ContentUserProfile it) ->
            "u1".equals(it.getUserId()) && Integer.valueOf(180).equals(it.getGrowthValue())));
        verify(decayStateMapper).updateById(argThat((ContentUserGrowthDecayState it) ->
            "u1".equals(it.getUserId())
                && Integer.valueOf(1).equals(it.getDecayCount())
                && runTime.equals(it.getLastDecayTime())
                && it.getRuleSnapshotJson().contains("\"decayRate\":0.10")));
    }

    @Test
    void shouldProtectFromNegativeGrowthAndAvoidDuplicateDecayInSameDay() {
        Date runTime = time(2026, 5, 19, 9, 0);
        ContentUserGrowthDecayState state = new ContentUserGrowthDecayState()
            .setUserId("u1")
            .setLastActiveTime(time(2026, 4, 1, 0, 0))
            .setLastDecayTime(time(2026, 5, 19, 1, 0))
            .setStatus("NORMAL");
        when(decayStateMapper.selectList(any())).thenReturn(List.of());
        when(profileMapper.selectList(any())).thenReturn(List.of(new ContentUserProfile()
            .setUserId("u1")
            .setGrowthValue(1)
            .setLevel(1)));
        when(deviceSessionMapper.selectLatestActiveTimeByUserId("u1")).thenReturn(time(2026, 4, 1, 0, 0));
        when(decayStateMapper.selectOne(any())).thenReturn(state);

        int affected = service.executeDecay(runTime, rule("0.50", true));

        assertThat(affected).isZero();
        verify(growthLedgerMapper, never()).insert(any(ContentUserGrowthLedger.class));
        verify(profileMapper, never()).updateById(any(ContentUserProfile.class));
    }

    @Test
    void shouldStartProtectionWhenDecayDropsBelowCurrentLevelThresholdAndRecoverOnActivity() {
        Date runTime = time(2026, 5, 19, 10, 0);
        when(decayStateMapper.selectList(any())).thenReturn(List.of());
        when(profileMapper.selectList(any())).thenReturn(List.of(new ContentUserProfile()
            .setUserId("u1")
            .setGrowthValue(205)
            .setLevel(3)));
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setGrowthValue(220)
            .setLevel(3));
        when(deviceSessionMapper.selectLatestActiveTimeByUserId("u1")).thenReturn(time(2026, 4, 1, 0, 0));
        ContentUserGrowthDecayState protectingState = new ContentUserGrowthDecayState()
            .setUserId("u1")
            .setStatus("PROTECTING")
            .setDecayCount(1)
            .setProtectionStartedAt(runTime)
            .setProtectionUntil(time(2026, 5, 26, 10, 0));
        protectingState.setId("decay-1");
        when(decayStateMapper.selectOne(any())).thenReturn(null, null, protectingState);

        service.executeDecay(runTime, rule("0.05", true));
        service.markUserActive("u1", time(2026, 5, 20, 10, 0), 220);

        verify(decayStateMapper).updateById(argThat((ContentUserGrowthDecayState it) ->
            "u1".equals(it.getUserId())
                && "PROTECTING".equals(it.getStatus())
                && it.getProtectionUntil() != null));
        verify(decayStateMapper).updateById(argThat((ContentUserGrowthDecayState it) ->
            "u1".equals(it.getUserId())
                && "NORMAL".equals(it.getStatus())
                && it.getProtectionUntil() == null));
    }

    @Test
    void shouldDowngradeAfterProtectionExpiresAndSkipWhenThresholdRecovered() {
        Date runTime = time(2026, 5, 27, 10, 0);
        ContentUserGrowthDecayState downgradeState = protecting("u1", time(2026, 5, 26, 10, 0));
        ContentUserGrowthDecayState recoveredState = protecting("u2", time(2026, 5, 26, 10, 0));
        when(decayStateMapper.selectList(any())).thenReturn(List.of(downgradeState, recoveredState), List.of());
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setGrowthValue(190)
            .setLevel(3));
        when(profileMapper.selectByUserId("u2")).thenReturn(new ContentUserProfile()
            .setUserId("u2")
            .setGrowthValue(230)
            .setLevel(3));
        when(profileMapper.selectList(any())).thenReturn(List.of());

        int affected = service.executeDecay(runTime);

        assertThat(affected).isEqualTo(1);
        verify(profileMapper).updateById(argThat((ContentUserProfile it) ->
            "u1".equals(it.getUserId()) && Integer.valueOf(2).equals(it.getLevel())));
        verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) ->
            "USER_LEVEL_DOWN".equals(it.getEventType())
                && "u1".equals(it.getUserId())
                && it.getExtraDataJson().contains("\"notification\":true")));
        verify(profileMapper, never()).updateById(argThat((ContentUserProfile it) -> "u2".equals(it.getUserId())));
    }

    @Test
    void shouldExposeRuleDescriptionAndRejectInvalidOrDisabledDecayConfig() {
        ContentUserGrowthDecayRuleVO rule = service.getDecayRule();

        assertThat(rule.getInactiveDays()).isEqualTo(30);
        assertThat(rule.getProtectionDays()).isEqualTo(7);
        assertThat(rule.getRuleDescription()).contains("30 天");
        assertThat(service.executeDecay(time(2026, 5, 19, 10, 0), rule("0.10", false))).isZero();
        assertThatThrownBy(() -> service.validateDecayRule(rule("-0.01", true)))
            .isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.validateDecayRule(rule("0.51", true)))
            .isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.validateDecayRule(rule("0.10", true).setInactiveDays(0)))
            .isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.validateDecayRule(rule("0.10", true).setProtectionDays(-1)))
            .isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.validateDecayRule(rule("0.10", true).setRuleDescription("x".repeat(513))))
            .isInstanceOf(JeecgBootException.class);
    }

    @Test
    void shouldReturnDecayStatusForUserWithDecayState() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u1")
            .setLevel(3)
            .setGrowthValue(200);
        when(profileMapper.selectByUserId("u1")).thenReturn(profile);
        ContentUserGrowthDecayState state = new ContentUserGrowthDecayState()
            .setUserId("u1")
            .setStatus("DECAYING")
            .setLastActiveTime(time(2026, 4, 1, 0, 0))
            .setDecayCount(2);
        when(decayStateMapper.selectOne(any())).thenReturn(state);

        ContentUserGrowthDecayStatusVO status = service.getDecayStatus("u1");

        assertThat(status.getStatus()).isEqualTo("DECAYING");
        assertThat(status.getCurrentLevel()).isEqualTo(3);
        assertThat(status.getCurrentGrowthValue()).isEqualTo(200);
        assertThat(status.getDecayCount()).isEqualTo(2);
        assertThat(status.getInactiveDays()).isGreaterThan(0);
    }

    @Test
    void shouldReturnNormalStatusForUserWithoutDecayState() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u2")
            .setLevel(1)
            .setGrowthValue(50);
        when(profileMapper.selectByUserId("u2")).thenReturn(profile);
        when(decayStateMapper.selectOne(any())).thenReturn(null);

        ContentUserGrowthDecayStatusVO status = service.getDecayStatus("u2");

        assertThat(status.getStatus()).isEqualTo("NORMAL");
        assertThat(status.getInactiveDays()).isZero();
        assertThat(status.getDecayCount()).isZero();
    }

    @Test
    void shouldRejectDecayStatusQueryWithBlankUserId() {
        assertThatThrownBy(() -> service.getDecayStatus(""))
            .isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.getDecayStatus(null))
            .isInstanceOf(JeecgBootException.class);
    }

    private ContentUserGrowthDecayRuleVO rule(String decayRate, boolean enabled) {
        return new ContentUserGrowthDecayRuleVO()
            .setEnabled(enabled)
            .setInactiveDays(30)
            .setDecayRate(new BigDecimal(decayRate))
            .setProtectionDays(7)
            .setRuleDescription("连续 30 天未登录后衰减，7 天保护");
    }

    private ContentUserGrowthDecayState protecting(String userId, Date protectionUntil) {
        ContentUserGrowthDecayState state = new ContentUserGrowthDecayState()
            .setUserId(userId)
            .setStatus("PROTECTING")
            .setProtectionUntil(protectionUntil);
        state.setId("decay-" + userId);
        return state;
    }

    private ContentUserLevelConfig level(int level, int threshold) {
        return new ContentUserLevelConfig()
            .setLevel(level)
            .setLevelName("Lv" + level)
            .setGrowthThreshold(threshold)
            .setEnabled(Boolean.TRUE);
    }

    private Date time(int year, int month, int day, int hour, int minute) {
        return Date.from(LocalDateTime.of(year, month, day, hour, minute)
            .atZone(ZoneId.systemDefault())
            .toInstant());
    }
}
