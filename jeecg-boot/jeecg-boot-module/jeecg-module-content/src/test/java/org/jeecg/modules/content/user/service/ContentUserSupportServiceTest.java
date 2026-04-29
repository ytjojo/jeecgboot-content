package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.mapper.ContentUserAppealMapper;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;
import org.jeecg.modules.content.user.service.impl.ContentUserSupportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContentUserSupportServiceTest {

    @Mock
    private ContentUserAppealMapper appealMapper;

    @Mock
    private ContentUserAuditLogMapper auditLogMapper;

    @InjectMocks
    private ContentUserSupportServiceImpl supportService;

    @Test
    void shouldCreateAppealAgainstPenaltyAndRecordProgress() {
        String appealId = supportService.createAppeal(createAppealReq());

        assertThat(appealId).isNotBlank();
        verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) -> "USER_APPEAL_CREATED".equals(it.getEventType())));
    }

    private ContentAppealCreateReq createAppealReq() {
        return new ContentAppealCreateReq()
            .setUserId("u1")
            .setAppealType("PENALTY")
            .setTargetId("penalty_1")
            .setTargetType("STATUS_RECORD")
            .setReason("需要复核")
            .setEvidenceJson("{\"proof\":true}");
    }
}
