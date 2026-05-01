package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
import org.jeecg.modules.content.user.entity.ContentUserAppeal;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.enums.ContentUserStatusEnum;
import org.jeecg.modules.content.user.gateway.SystemUserAccountGateway;
import org.jeecg.modules.content.user.mapper.ContentUserAppealMapper;
import org.jeecg.modules.content.user.mapper.ContentUserNotificationSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserStatusRecordMapper;
import org.jeecg.modules.content.user.req.account.ContentPasswordResetReq;
import org.jeecg.modules.content.user.req.account.ContentRegisterReq;
import org.jeecg.modules.content.user.service.impl.ContentAccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentAccountServiceTest {

    @Mock
    private SystemUserAccountGateway systemUserAccountGateway;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private ContentUserNotificationSettingMapper notificationSettingMapper;

    @Mock
    private ContentUserStatusRecordMapper statusRecordMapper;

    @Mock
    private ContentUserAppealMapper appealMapper;

    @InjectMocks
    private ContentAccountServiceImpl accountService;

    @Test
    void shouldCreateSysUserAndBootstrapCommunityProfile() {
        when(systemUserAccountGateway.createUser(any())).thenReturn("u_1001");

        String userId = accountService.registerByMobile(registerReq());

        assertThat(userId).isEqualTo("u_1001");
        verify(profileMapper).insert(any(ContentUserProfile.class));
        verify(notificationSettingMapper).insert(any(ContentUserNotificationSetting.class));
    }

    @Test
    void shouldEnterCancelPendingInsteadOfCancellingImmediately() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u_1001")
            .setStatus(ContentUserStatusEnum.NORMAL.getCode());
        when(profileMapper.selectByUserId("u_1001")).thenReturn(profile);

        accountService.initiateCancel("u_1001", "u_1001", "用户申请注销");

        ArgumentCaptor<ContentUserStatusRecord> recordCaptor = ArgumentCaptor.forClass(ContentUserStatusRecord.class);
        verify(statusRecordMapper).insert(recordCaptor.capture());
        verify(systemUserAccountGateway, never()).markCancelled("u_1001");
        verify(profileMapper).updateById(profile);

        ContentUserStatusRecord record = recordCaptor.getValue();
        assertThat(profile.getStatus()).isEqualTo(ContentUserStatusEnum.CANCEL_PENDING.getCode());
        assertThat(record.getUserId()).isEqualTo("u_1001");
        assertThat(record.getCurrentStatus()).isEqualTo(ContentUserStatusEnum.NORMAL.getCode());
        assertThat(record.getTargetStatus()).isEqualTo(ContentUserStatusEnum.CANCEL_PENDING.getCode());
        assertThat(record.getTriggerSource()).isEqualTo("USER_CANCEL_APPLY");
        assertThat(record.getRecoverable()).isTrue();
        assertThat(record.getEffectiveStartTime()).isNotNull();
        assertThat(record.getEffectiveEndTime()).isNotNull();
        assertThat(record.getEffectiveEndTime()).isAfter(record.getEffectiveStartTime());
    }

    @Test
    void shouldRejectCancelApplyWhenPendingAppealExists() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u_1001")
            .setStatus("PENDING");
        when(appealMapper.selectOne(any())).thenReturn(appeal);

        assertThatThrownBy(() -> accountService.initiateCancel("u_1001", "u_1001", "用户申请注销"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("存在待处理申诉，暂不可注销");
    }

    @Test
    void shouldRejectCompleteCancelBeforeCoolingPeriodEnds() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u_1001")
            .setStatus(ContentUserStatusEnum.CANCEL_PENDING.getCode());
        ContentUserStatusRecord latestRecord = new ContentUserStatusRecord()
            .setUserId("u_1001")
            .setCurrentStatus(ContentUserStatusEnum.NORMAL.getCode())
            .setTargetStatus(ContentUserStatusEnum.CANCEL_PENDING.getCode())
            .setEffectiveStartTime(new Date())
            .setEffectiveEndTime(new Date(System.currentTimeMillis() + 60_000));
        when(profileMapper.selectByUserId("u_1001")).thenReturn(profile);
        when(statusRecordMapper.selectLatestByUserId("u_1001")).thenReturn(latestRecord);

        assertThatThrownBy(() -> accountService.completeCancel("u_1001", "system"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("注销冷静期未结束");
    }

    @Test
    void shouldCompleteCancelAfterCoolingPeriodEnds() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u_1001")
            .setStatus(ContentUserStatusEnum.CANCEL_PENDING.getCode());
        ContentUserStatusRecord latestRecord = new ContentUserStatusRecord()
            .setUserId("u_1001")
            .setCurrentStatus(ContentUserStatusEnum.NORMAL.getCode())
            .setTargetStatus(ContentUserStatusEnum.CANCEL_PENDING.getCode())
            .setEffectiveStartTime(new Date(System.currentTimeMillis() - 120_000))
            .setEffectiveEndTime(new Date(System.currentTimeMillis() - 60_000));
        when(profileMapper.selectByUserId("u_1001")).thenReturn(profile);
        when(statusRecordMapper.selectLatestByUserId("u_1001")).thenReturn(latestRecord);

        accountService.completeCancel("u_1001", "system");

        verify(systemUserAccountGateway).markCancelled("u_1001");
        verify(profileMapper).updateById(profile);
        assertThat(profile.getStatus()).isEqualTo(ContentUserStatusEnum.CANCELLED.getCode());
    }

    @Test
    void shouldRestorePreviousStatusWhenCancelIsRevoked() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u_1001")
            .setStatus(ContentUserStatusEnum.CANCEL_PENDING.getCode());
        ContentUserStatusRecord latestRecord = new ContentUserStatusRecord()
            .setUserId("u_1001")
            .setCurrentStatus(ContentUserStatusEnum.NORMAL.getCode())
            .setTargetStatus(ContentUserStatusEnum.CANCEL_PENDING.getCode());
        when(profileMapper.selectByUserId("u_1001")).thenReturn(profile);
        when(statusRecordMapper.selectLatestByUserId("u_1001")).thenReturn(latestRecord);

        accountService.revokeCancel("u_1001", "u_1001", "继续使用账号");

        verify(profileMapper).updateById(profile);
        assertThat(profile.getStatus()).isEqualTo(ContentUserStatusEnum.NORMAL.getCode());
    }

    @Test
    void shouldRejectResetPasswordWithoutSecondaryVerification() {
        ContentPasswordResetReq req = new ContentPasswordResetReq()
            .setUserId("u_1001")
            .setNewPassword("Pass@123")
            .setSecondaryVerified(Boolean.FALSE);

        assertThatThrownBy(() -> accountService.resetPassword(req))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("密码重置需先完成二次校验");
        verify(systemUserAccountGateway, never()).resetPassword(any());
    }

    private ContentRegisterReq registerReq() {
        return new ContentRegisterReq()
            .setUsername("community_user_1001")
            .setMobile("13800000001")
            .setPassword("Pass@123")
            .setNickname("社区用户");
    }
}
