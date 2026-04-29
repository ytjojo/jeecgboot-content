package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.gateway.SystemUserAccountGateway;
import org.jeecg.modules.content.user.mapper.ContentUserNotificationSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.req.account.ContentRegisterReq;
import org.jeecg.modules.content.user.service.impl.ContentAccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

    private ContentRegisterReq registerReq() {
        return new ContentRegisterReq()
            .setUsername("community_user_1001")
            .setMobile("13800000001")
            .setPassword("Pass@123")
            .setNickname("社区用户");
    }
}
