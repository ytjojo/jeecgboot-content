package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.enums.ContentUserStatusEnum;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserStatusRecordMapper;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.service.impl.ContentUserGovernanceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ContentUserGovernanceServiceTest {

    @Mock
    private ContentUserStatusRecordMapper statusRecordMapper;

    @Mock
    private ContentUserAuditLogMapper auditLogMapper;

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

    private ContentUserStatusChangeReq changeReq(String userId, String targetStatus) {
        return new ContentUserStatusChangeReq()
            .setUserId(userId)
            .setCurrentStatus(ContentUserStatusEnum.NORMAL.getCode())
            .setTargetStatus(targetStatus)
            .setOperatorUserId("admin")
            .setReason("违规处理");
    }
}
