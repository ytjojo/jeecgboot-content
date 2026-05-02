package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.enums.ContentUserStatusEnum;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserStatusRecordMapper;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.service.impl.ContentUserGovernanceServiceImpl;
import org.jeecg.modules.content.user.vo.ContentUserStatusHistoryPageVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for content user governance service.
 */
@ExtendWith(MockitoExtension.class)
class ContentUserGovernanceServiceTest {

    @Mock
    private ContentUserStatusRecordMapper statusRecordMapper;

    @Mock
    private ContentUserAuditLogMapper auditLogMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private IContentUserGrowthPenaltyRecoveryService growthPenaltyRecoveryService;

    @Mock
    private IContentUserGrowthPenaltyRecordService growthPenaltyRecordService;

    @InjectMocks
    private ContentUserGovernanceServiceImpl governanceService;

    @Test
    void shouldRecordAuditWhenUserIsMuted() {
        governanceService.changeStatus(changeReq("u1", ContentUserStatusEnum.MUTED.getCode()));

        verify(statusRecordMapper).insert(any(ContentUserStatusRecord.class));
        verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) -> "USER_STATUS_CHANGE".equals(it.getEventType())));
    }

    @Test
    void shouldRejectIllegalStatusTransitionFromCancelledToNormal() {
        ContentUserStatusChangeReq req = new ContentUserStatusChangeReq()
            .setUserId("u1")
            .setCurrentStatus(ContentUserStatusEnum.CANCELLED.getCode())
            .setTargetStatus(ContentUserStatusEnum.NORMAL.getCode())
            .setOperatorUserId("admin")
            .setReason("非法恢复");

        assertThatThrownBy(() -> governanceService.changeStatus(req))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("状态流转");
        verifyNoInteractions(statusRecordMapper, auditLogMapper);
    }

    @Test
    void shouldCreateGrowthPenaltyRecordWhenStatusChangesToMuted() {
        governanceService.changeStatus(changeReq("u1", ContentUserStatusEnum.MUTED.getCode()));

        verify(growthPenaltyRecordService).createFromGovernanceRecord(
            argThat(it -> "u1".equals(it.getUserId()) && "MUTED".equals(it.getTargetStatus())),
            argThat(it -> "u1".equals(it.getUserId()) && "admin".equals(it.getOperatorUserId())),
            any(Date.class)
        );
    }

    @Test
    void shouldNotCreateGrowthPenaltyRecordWhenStatusChangesToNormal() {
        ContentUserStatusChangeReq req = new ContentUserStatusChangeReq()
            .setUserId("u1")
            .setCurrentStatus(ContentUserStatusEnum.REGISTERED_INCOMPLETE.getCode())
            .setTargetStatus(ContentUserStatusEnum.NORMAL.getCode())
            .setOperatorUserId("admin")
            .setReason("资料补全");

        governanceService.changeStatus(req);

        verifyNoInteractions(growthPenaltyRecordService);
    }

    @Test
    void shouldPageStatusHistoryByUser() {
        Date startTime = new Date(1735689600000L);
        Date endTime = new Date(1735693200000L);
        ContentUserStatusRecord record = new ContentUserStatusRecord()
            .setUserId("u1")
            .setCurrentStatus("NORMAL")
            .setTargetStatus("FROZEN")
            .setTriggerSource("MANUAL")
            .setOperatorUserId("admin-1")
            .setReason("违规处理")
            .setRuleCode("RULE-1")
            .setEffectiveStartTime(startTime)
            .setEffectiveEndTime(endTime)
            .setRecoverable(Boolean.TRUE);
        record.setId("record-1");
        record.setCreateTime(startTime);
        when(statusRecordMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            IPage<ContentUserStatusRecord> page = invocation.getArgument(0);
            page.setRecords(List.of(record));
            page.setTotal(3L);
            return page;
        });

        ContentUserStatusHistoryPageVO result = governanceService.listStatusHistory("u1", 2L, 1L);

        assertThat(result.getTotal()).isEqualTo(3L);
        assertThat(result.getPageNo()).isEqualTo(2L);
        assertThat(result.getPageSize()).isEqualTo(1L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getRecordId()).isEqualTo("record-1");
        assertThat(result.getRecords().get(0).getTargetStatus()).isEqualTo("FROZEN");
        verify(statusRecordMapper).selectPage(argThat(page -> page.getCurrent() == 2L && page.getSize() == 1L),
            argThat(wrapper -> wrapper != null));
    }

    @Test
    void shouldAutoRecoverExpiredGovernanceStatus() {
        Date currentTime = new Date(1735696800000L);
        Date endTime = new Date(1735693200000L);
        ContentUserStatusRecord expiredRecord = new ContentUserStatusRecord()
            .setUserId("u1")
            .setCurrentStatus("NORMAL")
            .setTargetStatus("FROZEN")
            .setTriggerSource("MANUAL")
            .setOperatorUserId("admin-1")
            .setReason("违规处理")
            .setEffectiveEndTime(endTime)
            .setRecoverable(Boolean.TRUE);
        expiredRecord.setId("record-1");
        when(statusRecordMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            IPage<ContentUserStatusRecord> page = invocation.getArgument(0);
            page.setRecords(List.of(expiredRecord));
            page.setTotal(1L);
            return page;
        });
        when(profileMapper.selectList(any())).thenReturn(List.of(new ContentUserProfile()
            .setUserId("u1")
            .setStatus("FROZEN")));

        int recoveredCount = governanceService.autoRecoverExpiredStatuses(currentTime, 50L);

        assertThat(recoveredCount).isEqualTo(1);
        verify(profileMapper).updateById(argThat((ContentUserProfile it) ->
            "u1".equals(it.getUserId())
                && "NORMAL".equals(it.getStatus())));
        verify(statusRecordMapper).insert(argThat((ContentUserStatusRecord it) ->
            "u1".equals(it.getUserId())
                && "FROZEN".equals(it.getCurrentStatus())
                && "NORMAL".equals(it.getTargetStatus())
                && "AUTO_EXPIRE_RECOVER".equals(it.getTriggerSource())
                && "system".equals(it.getOperatorUserId())));
        verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) ->
            "USER_STATUS_CHANGE".equals(it.getEventType())
                && "u1".equals(it.getUserId())
                && "system".equals(it.getOperatorUserId())));
    }

    @Test
    void shouldRecoverGrowthPenaltyAfterAutoRecoverGovernanceStatus() {
        Date currentTime = new Date(1735696800000L);
        Date endTime = new Date(1735693200000L);
        ContentUserStatusRecord expiredRecord = new ContentUserStatusRecord()
            .setUserId("u1")
            .setCurrentStatus("NORMAL")
            .setTargetStatus("FROZEN")
            .setEffectiveEndTime(endTime)
            .setRecoverable(Boolean.TRUE);
        expiredRecord.setId("record-1");
        when(statusRecordMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            IPage<ContentUserStatusRecord> page = invocation.getArgument(0);
            page.setRecords(List.of(expiredRecord));
            page.setTotal(1L);
            return page;
        });
        when(profileMapper.selectList(any())).thenReturn(List.of(new ContentUserProfile()
            .setUserId("u1")
            .setStatus("FROZEN")));

        governanceService.autoRecoverExpiredStatuses(currentTime, 50L);

        verify(growthPenaltyRecoveryService).recoverByGovernanceRecord(
            argThat(it -> "record-1".equals(it.getId()) && "u1".equals(it.getUserId())),
            argThat(it -> "system".equals(it)),
            argThat(it -> currentTime.equals(it)),
            argThat(it -> "处罚到期自动恢复".equals(it))
        );
    }

    private ContentUserStatusChangeReq changeReq(String userId, String targetStatus) {
        return new ContentUserStatusChangeReq()
            .setUserId(userId)
            .setCurrentStatus(ContentUserStatusEnum.NORMAL.getCode())
            .setTargetStatus(targetStatus)
            .setOperatorUserId("admin")
            .setReason("违规处理");
    }
}
