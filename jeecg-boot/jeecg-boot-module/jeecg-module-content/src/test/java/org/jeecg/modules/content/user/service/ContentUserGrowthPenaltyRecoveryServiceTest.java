package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserAppeal;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserBadgeGrant;
import org.jeecg.modules.content.user.entity.ContentUserGrowthLedger;
import org.jeecg.modules.content.user.entity.ContentUserGrowthPenaltyRecord;
import org.jeecg.modules.content.user.entity.ContentUserPointLedger;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserBadgeGrantMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthPenaltyRecordMapper;
import org.jeecg.modules.content.user.mapper.ContentUserPointLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserGrowthPenaltyRecoveryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for growth penalty recovery orchestration.
 */
@ExtendWith(MockitoExtension.class)
class ContentUserGrowthPenaltyRecoveryServiceTest {

    @Mock
    private ContentUserGrowthPenaltyRecordMapper growthPenaltyRecordMapper;

    @Mock
    private ContentUserPointLedgerMapper pointLedgerMapper;

    @Mock
    private ContentUserGrowthLedgerMapper growthLedgerMapper;

    @Mock
    private ContentUserBadgeGrantMapper badgeGrantMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private IContentUserLevelBenefitRecoveryService levelBenefitRecoveryService;

    @Mock
    private ContentUserAuditLogMapper auditLogMapper;

    @InjectMocks
    private ContentUserGrowthPenaltyRecoveryServiceImpl recoveryService;

    @Test
    void shouldRecoverPenaltyEffectsByAppealApproval() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setTargetType("GROWTH_PENALTY")
            .setTargetId("penalty-1")
            .setResultStatus("APPROVED");
        appeal.setId("appeal-1");
        when(growthPenaltyRecordMapper.selectList(any())).thenReturn(List.of(buildPenaltyRecord()));
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setPointBalance(80)
            .setGrowthValue(190)
            .setLevel(2));
        when(badgeGrantMapper.selectById("badge-grant-1")).thenReturn(new ContentUserBadgeGrant()
            .setUserId("u1")
            .setStatus("RECYCLED")
            .setDisplaying(Boolean.FALSE));

        int recoveredCount = recoveryService.recoverByAppeal(appeal, "admin-1", new Date(), "处罚撤销");

        assertThat(recoveredCount).isEqualTo(1);
        verify(pointLedgerMapper).insert(argThat((ContentUserPointLedger it) ->
            "u1".equals(it.getUserId())
                && it.getPointDelta() == 20
                && "PENALTY_RECOVER".equals(it.getSourceType())));
        verify(growthLedgerMapper).insert(argThat((ContentUserGrowthLedger it) ->
            "u1".equals(it.getUserId())
                && it.getGrowthDelta() == 10
                && "PENALTY_RECOVER".equals(it.getSourceType())));
        verify(profileMapper).updateById(argThat((ContentUserProfile it) ->
            "u1".equals(it.getUserId())
                && it.getPointBalance() == 100
                && it.getGrowthValue() == 200
                && it.getLevel() == 3));
        verify(badgeGrantMapper).updateById(argThat((ContentUserBadgeGrant it) ->
            "ACTIVE".equals(it.getStatus())
                && Boolean.TRUE.equals(it.getDisplaying())));
        verify(growthPenaltyRecordMapper).updateById(argThat((ContentUserGrowthPenaltyRecord it) ->
            "RECOVERED".equals(it.getStatus())
                && "APPEAL_APPROVED".equals(it.getRecoverTrigger())));
        verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) ->
            "USER_GROWTH_PENALTY_RECOVERED".equals(it.getEventType())
                && "u1".equals(it.getUserId())));
    }

    @Test
    void shouldSkipRecoveredPenaltyRecord() {
        when(growthPenaltyRecordMapper.selectList(any())).thenReturn(List.of(buildRecoveredPenaltyRecord()));

        int recoveredCount = recoveryService.recoverByAppeal(
            new ContentUserAppeal()
                .setUserId("u1")
                .setTargetType("GROWTH_PENALTY")
                .setTargetId("penalty-2")
                .setResultStatus("APPROVED"),
            "admin-1",
            new Date(),
            "重复触发"
        );

        assertThat(recoveredCount).isEqualTo(0);
        verifyNoInteractions(pointLedgerMapper, growthLedgerMapper, badgeGrantMapper, auditLogMapper);
    }

    @Test
    void shouldContinueWhenBadgeGrantIsMissing() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setTargetType("GROWTH_PENALTY")
            .setTargetId("penalty-1")
            .setResultStatus("APPROVED");
        appeal.setId("appeal-1");
        when(growthPenaltyRecordMapper.selectList(any())).thenReturn(List.of(buildPenaltyRecord()));
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setPointBalance(0)
            .setGrowthValue(0)
            .setLevel(1));
        when(badgeGrantMapper.selectById("badge-grant-1")).thenReturn(null);

        int recoveredCount = recoveryService.recoverByAppeal(appeal, "admin-1", new Date(), "处罚撤销");

        assertThat(recoveredCount).isEqualTo(1);
        verify(pointLedgerMapper).insert(any(ContentUserPointLedger.class));
        verify(growthLedgerMapper).insert(any(ContentUserGrowthLedger.class));
    }

    @Test
    void shouldRecoverLevelBenefitWhenAppealApproved() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setTargetType("GROWTH_PENALTY")
            .setTargetId("penalty-1")
            .setResultStatus("APPROVED");
        appeal.setId("appeal-1");
        when(growthPenaltyRecordMapper.selectList(any())).thenReturn(List.of(buildPenaltyRecord()));
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setPointBalance(80)
            .setGrowthValue(190)
            .setLevel(2));
        when(levelBenefitRecoveryService.recoverByPenaltyRecord(any(), any(), any(), any())).thenReturn(1);

        int recoveredCount = recoveryService.recoverByAppeal(appeal, "admin-1", new Date(), "处罚撤销");

        assertThat(recoveredCount).isEqualTo(1);
        verify(levelBenefitRecoveryService).recoverByPenaltyRecord(
            argThat(it -> "penalty-1".equals(it.getId()) && "u1".equals(it.getUserId())),
            argThat(it -> "admin-1".equals(it)),
            any(Date.class),
            argThat(it -> "处罚撤销".equals(it))
        );
        verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) ->
            "USER_GROWTH_PENALTY_RECOVERED".equals(it.getEventType())
                && it.getExtraDataJson().contains("\"recoveredBenefitCount\":1")));
    }

    @Test
    void shouldRecoverPenaltyEffectsFromExecutionSnapshot() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setTargetType("GROWTH_PENALTY")
            .setTargetId("penalty-3")
            .setResultStatus("APPROVED");
        appeal.setId("appeal-1");
        when(growthPenaltyRecordMapper.selectList(any())).thenReturn(List.of(buildExecutionSnapshotPenaltyRecord()));
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setPointBalance(80)
            .setGrowthValue(230)
            .setLevel(3));
        when(badgeGrantMapper.selectById("badge-grant-1")).thenReturn(new ContentUserBadgeGrant()
            .setUserId("u1")
            .setStatus("RECYCLED")
            .setDisplaying(Boolean.FALSE));
        when(levelBenefitRecoveryService.recoverByPenaltyRecord(any(), any(), any(), any())).thenReturn(1);

        int recoveredCount = recoveryService.recoverByAppeal(appeal, "admin-1", new Date(), "处罚撤销");

        assertThat(recoveredCount).isEqualTo(1);
        verify(pointLedgerMapper).insert(argThat((ContentUserPointLedger it) ->
            "u1".equals(it.getUserId())
                && it.getPointDelta() == 20
                && "PENALTY_RECOVER".equals(it.getSourceType())));
        verify(growthLedgerMapper).insert(argThat((ContentUserGrowthLedger it) ->
            "u1".equals(it.getUserId())
                && it.getGrowthDelta() == 30
                && "PENALTY_RECOVER".equals(it.getSourceType())));
        verify(profileMapper).updateById(argThat((ContentUserProfile it) ->
            "u1".equals(it.getUserId())
                && it.getPointBalance() == 100
                && it.getGrowthValue() == 260
                && it.getLevel() == 3));
        verify(badgeGrantMapper).updateById(argThat((ContentUserBadgeGrant it) ->
            "ACTIVE".equals(it.getStatus())
                && Boolean.TRUE.equals(it.getDisplaying())));
    }

    private ContentUserGrowthPenaltyRecord buildPenaltyRecord() {
        ContentUserGrowthPenaltyRecord record = new ContentUserGrowthPenaltyRecord()
            .setUserId("u1")
            .setPenaltyType("COMPOSITE")
            .setStatus("PENDING_RECOVER")
            .setEffectSnapshotJson("{\"pointDelta\":-20,\"growthDelta\":-10,"
                + "\"badgeEffects\":[{\"badgeGrantId\":\"badge-grant-1\",\"previousStatus\":\"ACTIVE\","
                + "\"previousDisplaying\":true}]}");
        record.setId("penalty-1");
        return record;
    }

    private ContentUserGrowthPenaltyRecord buildRecoveredPenaltyRecord() {
        ContentUserGrowthPenaltyRecord record = new ContentUserGrowthPenaltyRecord()
            .setUserId("u1")
            .setPenaltyType("POINT_DEDUCT")
            .setStatus("RECOVERED")
            .setRecoverTrigger("APPEAL_APPROVED")
            .setEffectSnapshotJson("{\"pointDelta\":-20}");
        record.setId("penalty-2");
        return record;
    }

    private ContentUserGrowthPenaltyRecord buildExecutionSnapshotPenaltyRecord() {
        ContentUserGrowthPenaltyRecord record = new ContentUserGrowthPenaltyRecord()
            .setUserId("u1")
            .setPenaltyType("COMPOSITE")
            .setStatus("PENDING_RECOVER")
            .setEffectSnapshotJson("{\"pointEffect\":{\"delta\":-20,\"balanceBefore\":100,\"balanceAfter\":80},"
                + "\"growthEffect\":{\"delta\":-30,\"growthBefore\":260,\"growthAfter\":230,"
                + "\"levelBefore\":3,\"levelAfter\":3},"
                + "\"badgeEffects\":[{\"badgeGrantId\":\"badge-grant-1\",\"previousStatus\":\"ACTIVE\","
                + "\"previousDisplaying\":true}],"
                + "\"benefitEffects\":[{\"benefitCode\":\"PRIORITY_CUSTOMER_SERVICE\","
                + "\"previousEnabled\":true,\"currentEnabled\":false}]}");
        record.setId("penalty-3");
        return record;
    }
}
