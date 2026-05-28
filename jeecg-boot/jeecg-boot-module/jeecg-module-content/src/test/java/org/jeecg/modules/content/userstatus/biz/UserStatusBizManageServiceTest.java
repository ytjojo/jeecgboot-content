package org.jeecg.modules.content.userstatus.biz;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserStatusRecordMapper;
import org.jeecg.modules.content.userstatus.entity.UserStatusAuditLog;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.jeecg.modules.content.userstatus.service.UserStatusAuditLogService;
import org.jeecg.modules.content.userstatus.service.UserStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

/**
 * 状态变更编排测试。
 * 测试状态变更 + 审计日志在同一事务、事务回滚验证。
 */
@ExtendWith(MockitoExtension.class)
class UserStatusBizManageServiceTest {

    @Mock
    private UserStatusService userStatusService;

    @Mock
    private UserStatusAuditLogService auditLogService;

    @Mock
    private ContentUserProfileMapper userProfileMapper;

    @Mock
    private ContentUserStatusRecordMapper statusRecordMapper;

    @InjectMocks
    private UserStatusBizManageService bizManageService;

    @Test
    void shouldChangeStatusAndWriteAuditLog() {
        // Given
        String userId = "user001";
        UserStatusEnum fromStatus = UserStatusEnum.NORMAL;
        UserStatusEnum toStatus = UserStatusEnum.MUTED;
        String reason = "违规发言";
        String operatorId = "admin001";
        String operatorType = "ADMIN";
        String ipAddress = "192.168.1.1";

        ContentUserProfile profile = new ContentUserProfile();
        profile.setUserId(userId);
        profile.setStatus("NORMAL");
        when(userProfileMapper.selectByUserId(userId)).thenReturn(profile);
        doReturn(1).when(userProfileMapper).updateById((ContentUserProfile) any());
        doReturn(1).when(statusRecordMapper).insert((ContentUserStatusRecord) any());

        // When
        bizManageService.changeStatus(userId, fromStatus, toStatus, reason, operatorId, operatorType, ipAddress);

        // Then
        verify(userStatusService).validateStatusChange(fromStatus, toStatus, reason, false);
        ArgumentCaptor<ContentUserProfile> profileCaptor = ArgumentCaptor.forClass(ContentUserProfile.class);
        verify(userProfileMapper).updateById(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getStatus()).isEqualTo("MUTED");
        verify(statusRecordMapper).insert(any(ContentUserStatusRecord.class));
        verify(auditLogService).writeAuditLog(any(UserStatusAuditLog.class));
    }

    @Test
    void shouldRejectInvalidStatusChange() {
        // Given
        String userId = "user001";
        UserStatusEnum fromStatus = UserStatusEnum.GUEST;
        UserStatusEnum toStatus = UserStatusEnum.BANNED;
        String reason = "测试";

        doThrow(new JeecgBootException("非法的状态转换"))
            .when(userStatusService).validateStatusChange(fromStatus, toStatus, reason, false);

        // When & Then
        assertThatThrownBy(() ->
            bizManageService.changeStatus(userId, fromStatus, toStatus, reason, "admin001", "ADMIN", "192.168.1.1")
        ).isInstanceOf(JeecgBootException.class)
         .hasMessageContaining("非法的状态转换");

        verify(auditLogService, never()).writeAuditLog(any(UserStatusAuditLog.class));
    }

    @Test
    void shouldAllowAdminForceStatusChange() {
        // Given
        String userId = "user001";
        UserStatusEnum fromStatus = UserStatusEnum.GUEST;
        UserStatusEnum toStatus = UserStatusEnum.BANNED;
        String reason = "管理员强制封禁";
        String operatorId = "admin001";
        String operatorType = "ADMIN";
        String ipAddress = "192.168.1.1";

        ContentUserProfile profile = new ContentUserProfile();
        profile.setUserId(userId);
        profile.setStatus("GUEST");
        when(userProfileMapper.selectByUserId(userId)).thenReturn(profile);
        doReturn(1).when(userProfileMapper).updateById((ContentUserProfile) any());
        doReturn(1).when(statusRecordMapper).insert((ContentUserStatusRecord) any());

        // When
        bizManageService.forceChangeStatus(userId, fromStatus, toStatus, reason, operatorId, operatorType, ipAddress);

        // Then
        verify(userStatusService).validateStatusChange(fromStatus, toStatus, reason, true);
        ArgumentCaptor<ContentUserProfile> profileCaptor = ArgumentCaptor.forClass(ContentUserProfile.class);
        verify(userProfileMapper).updateById(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getStatus()).isEqualTo("BANNED");
        verify(auditLogService).writeAuditLog(any(UserStatusAuditLog.class));
    }

    @Test
    void shouldRollbackWhenAuditLogWriteFails() {
        // Given
        String userId = "user001";
        UserStatusEnum fromStatus = UserStatusEnum.NORMAL;
        UserStatusEnum toStatus = UserStatusEnum.MUTED;
        String reason = "违规发言";

        ContentUserProfile profile = new ContentUserProfile();
        profile.setUserId(userId);
        profile.setStatus("NORMAL");
        when(userProfileMapper.selectByUserId(userId)).thenReturn(profile);
        doReturn(1).when(userProfileMapper).updateById((ContentUserProfile) any());
        doReturn(1).when(statusRecordMapper).insert((ContentUserStatusRecord) any());

        doThrow(new RuntimeException("数据库异常"))
            .when(auditLogService).writeAuditLog(any(UserStatusAuditLog.class));

        // When & Then
        assertThatThrownBy(() ->
            bizManageService.changeStatus(userId, fromStatus, toStatus, reason, "admin001", "ADMIN", "192.168.1.1")
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("数据库异常");
    }

    @Test
    void shouldWriteCorrectAuditLogFields() {
        // Given
        String userId = "user001";
        UserStatusEnum fromStatus = UserStatusEnum.NORMAL;
        UserStatusEnum toStatus = UserStatusEnum.MUTED;
        String reason = "违规发言";
        String operatorId = "admin001";
        String operatorType = "ADMIN";
        String ipAddress = "192.168.1.1";

        ContentUserProfile profile = new ContentUserProfile();
        profile.setUserId(userId);
        profile.setStatus("NORMAL");
        when(userProfileMapper.selectByUserId(userId)).thenReturn(profile);
        doReturn(1).when(userProfileMapper).updateById((ContentUserProfile) any());
        doReturn(1).when(statusRecordMapper).insert((ContentUserStatusRecord) any());

        // When
        bizManageService.changeStatus(userId, fromStatus, toStatus, reason, operatorId, operatorType, ipAddress);

        // Then
        ArgumentCaptor<ContentUserProfile> profileCaptor = ArgumentCaptor.forClass(ContentUserProfile.class);
        verify(userProfileMapper).updateById(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getStatus()).isEqualTo("MUTED");

        ArgumentCaptor<ContentUserStatusRecord> recordCaptor = ArgumentCaptor.forClass(ContentUserStatusRecord.class);
        verify(statusRecordMapper).insert(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getCurrentStatus()).isEqualTo("NORMAL");
        assertThat(recordCaptor.getValue().getTargetStatus()).isEqualTo("MUTED");

        ArgumentCaptor<UserStatusAuditLog> auditCaptor = ArgumentCaptor.forClass(UserStatusAuditLog.class);
        verify(auditLogService).writeAuditLog(auditCaptor.capture());
        UserStatusAuditLog capturedLog = auditCaptor.getValue();
        assertThat(capturedLog.getUserId()).isEqualTo(userId);
        assertThat(capturedLog.getFromStatus()).isEqualTo(fromStatus.name());
        assertThat(capturedLog.getToStatus()).isEqualTo(toStatus.name());
        assertThat(capturedLog.getOperatorId()).isEqualTo(operatorId);
        assertThat(capturedLog.getOperatorType()).isEqualTo(operatorType);
        assertThat(capturedLog.getTriggerReason()).isEqualTo(reason);
        assertThat(capturedLog.getIpAddress()).isEqualTo(ipAddress);
    }
}
