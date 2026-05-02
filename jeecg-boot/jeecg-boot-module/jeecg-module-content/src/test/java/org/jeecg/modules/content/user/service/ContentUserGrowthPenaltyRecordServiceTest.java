package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserBadgeGrant;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserGrowthLedger;
import org.jeecg.modules.content.user.entity.ContentUserGrowthPenaltyRecord;
import org.jeecg.modules.content.user.entity.ContentUserLevelBenefitPenaltyRecord;
import org.jeecg.modules.content.user.entity.ContentUserPointLedger;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserReport;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserBadgeGrantMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthPenaltyRecordMapper;
import org.jeecg.modules.content.user.mapper.ContentUserLevelBenefitPenaltyRecordMapper;
import org.jeecg.modules.content.user.mapper.ContentUserPointLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitService;
import org.jeecg.modules.content.user.service.impl.ContentUserGrowthPenaltyRecordServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for growth penalty record creation service.
 */
@ExtendWith(MockitoExtension.class)
class ContentUserGrowthPenaltyRecordServiceTest {

    @Mock
    private ContentUserGrowthPenaltyRecordMapper growthPenaltyRecordMapper;

    @Mock
    private ContentUserPointLedgerMapper pointLedgerMapper;

    @Mock
    private ContentUserGrowthLedgerMapper growthLedgerMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private ContentUserBadgeGrantMapper badgeGrantMapper;

    @Mock
    private ContentUserLevelBenefitPenaltyRecordMapper levelBenefitPenaltyRecordMapper;

    @Mock
    private ContentUserAuditLogMapper auditLogMapper;

    @Mock
    private IContentUserLevelBenefitService levelBenefitService;

    @InjectMocks
    private ContentUserGrowthPenaltyRecordServiceImpl growthPenaltyRecordService;

    @Test
    void shouldCreateRecordFromGovernanceRecord() {
        ContentUserStatusRecord record = new ContentUserStatusRecord()
            .setUserId("u1")
            .setCurrentStatus("NORMAL")
            .setTargetStatus("MUTED")
            .setReason("违规处理")
            .setRuleCode("RULE-1");
        record.setId("status-1");
        ContentUserStatusChangeReq req = new ContentUserStatusChangeReq()
            .setUserId("u1")
            .setCurrentStatus("NORMAL")
            .setTargetStatus("MUTED")
            .setOperatorUserId("admin-1")
            .setReason("违规处理")
            .setRuleCode("RULE-1");
        when(growthPenaltyRecordMapper.selectOne(any())).thenReturn(null);
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setPointBalance(100)
            .setGrowthValue(260)
            .setLevel(3));
        when(badgeGrantMapper.selectList(any())).thenReturn(List.of());

        growthPenaltyRecordService.createFromGovernanceRecord(record, req, new Date(1735696800000L));

        verify(growthPenaltyRecordMapper).insert(argThat((ContentUserGrowthPenaltyRecord it) ->
            "u1".equals(it.getUserId())
                && "status-1".equals(it.getGovernanceRecordId())
                && "GOVERNANCE_STATUS_CHANGE".equals(it.getSourceType())
                && "status-1".equals(it.getSourceId())
                && "MUTED".equals(it.getSourceStatus())
                && "COMPOSITE".equals(it.getPenaltyType())
                && "PENDING_RECOVER".equals(it.getStatus())
                && it.getEffectSnapshotJson().contains("\"plannedEffects\"")
                && it.getEffectSnapshotJson().contains("\"pointEffect\"")
                && it.getEffectSnapshotJson().contains("\"growthEffect\"")));
    }

    @Test
    void shouldNotCreateRecordTwiceFromReportHandle() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setStatus("RESOLVED")
            .setResultStatus("CONFIRMED")
            .setReportType("SPAM");
        report.setId("report-1");
        when(growthPenaltyRecordMapper.selectOne(any())).thenReturn(new ContentUserGrowthPenaltyRecord()
            .setSourceType("REPORT_HANDLE")
            .setSourceId("report-1")
            .setStatus("PENDING_RECOVER"));

        growthPenaltyRecordService.createFromReportHandle(report, createHandleReportReq(), null, new Date());

        verify(growthPenaltyRecordMapper, never()).insert(any(ContentUserGrowthPenaltyRecord.class));
    }

    @Test
    void shouldExecutePenaltyFromGovernanceRecord() {
        ContentUserStatusRecord record = new ContentUserStatusRecord()
            .setUserId("u1")
            .setCurrentStatus("NORMAL")
            .setTargetStatus("MUTED")
            .setReason("违规处理")
            .setRuleCode("RULE-1");
        record.setId("status-2");
        ContentUserStatusChangeReq req = new ContentUserStatusChangeReq()
            .setUserId("u1")
            .setCurrentStatus("NORMAL")
            .setTargetStatus("MUTED")
            .setOperatorUserId("admin-1")
            .setReason("违规处理")
            .setRuleCode("RULE-1");
        when(growthPenaltyRecordMapper.selectOne(any())).thenReturn(null);
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setPointBalance(100)
            .setGrowthValue(260)
            .setLevel(3));
        when(badgeGrantMapper.selectList(any())).thenReturn(List.of());

        growthPenaltyRecordService.createFromGovernanceRecord(record, req, new Date(1735696800000L));

        verify(pointLedgerMapper).insert(argThat((ContentUserPointLedger it) ->
            "u1".equals(it.getUserId())
                && Integer.valueOf(-20).equals(it.getPointDelta())
                && "GROWTH_PENALTY".equals(it.getSourceType())));
        verify(growthLedgerMapper).insert(argThat((ContentUserGrowthLedger it) ->
            "u1".equals(it.getUserId())
                && Integer.valueOf(-30).equals(it.getGrowthDelta())
                && "GROWTH_PENALTY".equals(it.getSourceType())));
        verify(profileMapper).updateById(argThat((ContentUserProfile it) ->
            "u1".equals(it.getUserId())
                && Integer.valueOf(80).equals(it.getPointBalance())
                && Integer.valueOf(230).equals(it.getGrowthValue())
                && Integer.valueOf(3).equals(it.getLevel())));
        verify(growthPenaltyRecordMapper).insert(argThat((ContentUserGrowthPenaltyRecord it) ->
            "COMPOSITE".equals(it.getPenaltyType())
                && it.getEffectSnapshotJson().contains("\"pointEffect\"")
                && it.getEffectSnapshotJson().contains("\"growthEffect\"")));
        verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) ->
            "USER_GROWTH_PENALTY_EXECUTED".equals(it.getEventType())
                && "u1".equals(it.getUserId())
                && it.getExtraDataJson().contains("\"benefitCount\":1")));
    }

    @Test
    void shouldDisableBadgeAndLevelBenefitWhenPenaltyExecuted() {
        ContentUserStatusRecord record = new ContentUserStatusRecord()
            .setUserId("u1")
            .setCurrentStatus("NORMAL")
            .setTargetStatus("FROZEN");
        record.setId("status-3");
        when(growthPenaltyRecordMapper.selectOne(any())).thenReturn(null);
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setPointBalance(50)
            .setGrowthValue(120)
            .setLevel(2));
        when(levelBenefitService.hasEnabledBenefit("u1", "PRIORITY_CUSTOMER_SERVICE")).thenReturn(true);
        when(badgeGrantMapper.selectList(any())).thenReturn(List.of(createActiveBadgeGrant()));

        growthPenaltyRecordService.createFromGovernanceRecord(
            record,
            new ContentUserStatusChangeReq().setUserId("u1").setOperatorUserId("admin-1").setReason("违规处理"),
            new Date()
        );

        verify(badgeGrantMapper).updateById(argThat((ContentUserBadgeGrant it) ->
            "badge-grant-1".equals(it.getId())
                && "RECYCLED".equals(it.getStatus())
                && Boolean.FALSE.equals(it.getDisplaying())));
        verify(levelBenefitPenaltyRecordMapper).insert(argThat((ContentUserLevelBenefitPenaltyRecord it) ->
            "u1".equals(it.getUserId())
                && "PRIORITY_CUSTOMER_SERVICE".equals(it.getBenefitCode())
                && Boolean.TRUE.equals(it.getPreviousEnabled())
                && Boolean.FALSE.equals(it.getCurrentEnabled())));
    }

    @Test
    void shouldPersistDisabledBenefitWhenPriorityCustomerServiceWasNotEnabled() {
        ContentUserStatusRecord record = new ContentUserStatusRecord()
            .setUserId("u1")
            .setCurrentStatus("NORMAL")
            .setTargetStatus("MUTED");
        record.setId("status-5");
        when(growthPenaltyRecordMapper.selectOne(any())).thenReturn(null);
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setPointBalance(20)
            .setGrowthValue(50)
            .setLevel(1));
        when(levelBenefitService.hasEnabledBenefit("u1", "PRIORITY_CUSTOMER_SERVICE")).thenReturn(false);
        when(badgeGrantMapper.selectList(any())).thenReturn(List.of());

        growthPenaltyRecordService.createFromGovernanceRecord(
            record,
            new ContentUserStatusChangeReq().setUserId("u1").setOperatorUserId("admin-1"),
            new Date()
        );

        verify(levelBenefitPenaltyRecordMapper).insert(argThat((ContentUserLevelBenefitPenaltyRecord it) ->
            "u1".equals(it.getUserId())
                && "PRIORITY_CUSTOMER_SERVICE".equals(it.getBenefitCode())
                && Boolean.FALSE.equals(it.getPreviousEnabled())
                && Boolean.FALSE.equals(it.getCurrentEnabled())));
    }

    @Test
    void shouldSkipPenaltyExecutionWhenGovernanceSourceAlreadyExists() {
        ContentUserStatusRecord record = new ContentUserStatusRecord()
            .setUserId("u1")
            .setTargetStatus("MUTED");
        record.setId("status-4");
        when(growthPenaltyRecordMapper.selectOne(any())).thenReturn(new ContentUserGrowthPenaltyRecord()
            .setGovernanceRecordId("status-4")
            .setStatus("PENDING_RECOVER"));

        growthPenaltyRecordService.createFromGovernanceRecord(
            record,
            new ContentUserStatusChangeReq().setUserId("u1").setOperatorUserId("admin-1"),
            new Date()
        );

        verifyNoInteractions(pointLedgerMapper, growthLedgerMapper, profileMapper, badgeGrantMapper,
            levelBenefitPenaltyRecordMapper);
    }

    private ContentReportHandleReq createHandleReportReq() {
        return new ContentReportHandleReq()
            .setReportId("report-1")
            .setOperatorUserId("admin-1")
            .setStatus("RESOLVED")
            .setResultStatus("CONFIRMED")
            .setResultNote("违规成立")
            .setProgressNote("已处理完成");
    }

    private ContentUserBadgeGrant createActiveBadgeGrant() {
        ContentUserBadgeGrant badgeGrant = new ContentUserBadgeGrant()
            .setUserId("u1")
            .setBadgeCode("CREATOR_STAR")
            .setStatus("ACTIVE")
            .setDisplaying(Boolean.TRUE);
        badgeGrant.setId("badge-grant-1");
        return badgeGrant;
    }
}
