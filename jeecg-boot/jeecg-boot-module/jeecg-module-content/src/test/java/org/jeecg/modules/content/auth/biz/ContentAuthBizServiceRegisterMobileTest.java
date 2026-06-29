package org.jeecg.modules.content.auth.biz;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.auth.dto.AuthLoginResult;
import org.jeecg.modules.content.auth.entity.ContentUserAccount;
import org.jeecg.modules.content.auth.entity.ContentUserCredential;
import org.jeecg.modules.content.auth.enums.CredentialTypeEnum;
import org.jeecg.modules.content.auth.enums.VerificationCodeSceneEnum;
import org.jeecg.modules.content.auth.mapper.ContentUserAccountMapper;
import org.jeecg.modules.content.auth.mapper.ContentUserCredentialMapper;
import org.jeecg.modules.content.auth.req.ContentAuthMobileRegisterReq;
import org.jeecg.modules.content.auth.service.IContentVerificationCodeService;
import org.jeecg.modules.content.auth.service.LoginTokenGeneratorPort;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.gateway.SystemUserAccountGateway;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.jeecg.modules.content.user.mapper.ContentUserNotificationSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 手机号验证码注册业务编排测试。
 * 覆盖场景：注册成功、格式错误、重复注册、验证码过期/无效。
 */
@ExtendWith(MockitoExtension.class)
class ContentAuthBizServiceRegisterMobileTest {

    @Mock
    private IContentVerificationCodeService codeService;
    @Mock
    private ContentUserCredentialMapper credentialMapper;
    @Mock
    private ContentUserAccountMapper accountMapper;
    @Mock
    private SystemUserAccountGateway gateway;
    @Mock
    private ContentUserProfileMapper profileMapper;
    @Mock
    private ContentUserNotificationSettingMapper notificationMapper;
    @Mock
    private LoginTokenGeneratorPort loginTokenGeneratorPort;
    @Mock
    private ContentUserDeviceSessionMapper deviceSessionMapper;

    @InjectMocks
    private ContentAuthBizServiceImpl bizService;

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // ========== 注册成功 ==========

    @Test
    void registerByMobile_success_shouldCreateAccountCredentialProfileAndReturnUserId() {
        // given
        ContentAuthMobileRegisterReq req = buildValidReq();
        when(codeService.verifyCode(VerificationCodeSceneEnum.REGISTER, "13800138000", "123456")).thenReturn(true);
        when(credentialMapper.selectCount(any())).thenReturn(0L);
        when(gateway.createUser(any())).thenReturn("u_generated_001");
        when(loginTokenGeneratorPort.generateToken("u_generated_001", "PC")).thenReturn("access-token-xyz");
        when(deviceSessionMapper.insert(any(ContentUserDeviceSession.class))).thenReturn(1);

        // when
        AuthLoginResult result = bizService.registerByMobile(req);

        // then
        assertThat(result.getUserId()).isEqualTo("u_generated_001");
        assertThat(result.getAccessToken()).isEqualTo("access-token-xyz");

        // 验证凭证记录被创建
        ArgumentCaptor<ContentUserCredential> credentialCaptor = ArgumentCaptor.forClass(ContentUserCredential.class);
        verify(credentialMapper).insert(credentialCaptor.capture());
        ContentUserCredential savedCredential = credentialCaptor.getValue();
        assertThat(savedCredential.getUserId()).isEqualTo("u_generated_001");
        assertThat(savedCredential.getCredentialType()).isEqualTo(CredentialTypeEnum.SMS_CODE.getCode());
        assertThat(savedCredential.getCredentialValue()).isEqualTo("13800138000");
        assertThat(savedCredential.getStatus()).isEqualTo("ACTIVE");

        // 验证账号记录被创建
        ArgumentCaptor<ContentUserAccount> accountCaptor = ArgumentCaptor.forClass(ContentUserAccount.class);
        verify(accountMapper).insert(accountCaptor.capture());
        ContentUserAccount savedAccount = accountCaptor.getValue();
        assertThat(savedAccount.getUserId()).isEqualTo("u_generated_001");
        assertThat(savedAccount.getNickname()).isEqualTo("testUser");
        assertThat(savedAccount.getAccountStatus()).isEqualTo("ACTIVE");

        // 验证用户资料被创建
        ArgumentCaptor<ContentUserProfile> profileCaptor = ArgumentCaptor.forClass(ContentUserProfile.class);
        verify(profileMapper).insert(profileCaptor.capture());
        ContentUserProfile savedProfile = profileCaptor.getValue();
        assertThat(savedProfile.getUserId()).isEqualTo("u_generated_001");
        assertThat(savedProfile.getNickname()).isEqualTo("testUser");

        // 验证通知设置被创建
        verify(notificationMapper).insert(any(org.jeecg.modules.content.user.entity.ContentUserNotificationSetting.class));
    }

    // ========== 手机号格式错误（通过 @Pattern 校验） ==========

    @Test
    void registerByMobile_invalidMobileFormat_shouldFailValidation() {
        ContentAuthMobileRegisterReq req = new ContentAuthMobileRegisterReq()
                .setMobile("12345")  // 格式错误
                .setCode("123456")
                .setNickname("testUser");

        Set<ConstraintViolation<ContentAuthMobileRegisterReq>> violations = validator.validate(req);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("手机号格式不正确"));
    }

    @Test
    void registerByMobile_mobileNotStartingWith1_shouldFailValidation() {
        ContentAuthMobileRegisterReq req = new ContentAuthMobileRegisterReq()
                .setMobile("23800138000")  // 不以1开头
                .setCode("123456")
                .setNickname("testUser");

        Set<ConstraintViolation<ContentAuthMobileRegisterReq>> violations = validator.validate(req);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("手机号格式不正确"));
    }

    // ========== 重复注册 ==========

    @Test
    void registerByMobile_duplicateMobile_shouldThrowException() {
        // given
        ContentAuthMobileRegisterReq req = buildValidReq();
        when(codeService.verifyCode(VerificationCodeSceneEnum.REGISTER, "13800138000", "123456")).thenReturn(true);
        when(credentialMapper.selectCount(any())).thenReturn(1L);  // 已存在

        // when & then
        assertThatThrownBy(() -> bizService.registerByMobile(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("该手机号已被注册");
    }

    // ========== 验证码过期/无效 ==========

    @Test
    void registerByMobile_expiredCode_shouldThrowException() {
        // given
        ContentAuthMobileRegisterReq req = buildValidReq();
        when(codeService.verifyCode(VerificationCodeSceneEnum.REGISTER, "13800138000", "123456")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> bizService.registerByMobile(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("验证码无效或已过期");
    }

    @Test
    void registerByMobile_wrongCode_shouldThrowException() {
        // given
        ContentAuthMobileRegisterReq req = new ContentAuthMobileRegisterReq()
                .setMobile("13800138000")
                .setCode("000000")
                .setNickname("testUser");
        when(codeService.verifyCode(VerificationCodeSceneEnum.REGISTER, "13800138000", "000000")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> bizService.registerByMobile(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("验证码无效或已过期");
    }

    // ========== 验证码为空（通过 @NotBlank + @Size 校验） ==========

    @Test
    void registerByMobile_nullCode_shouldFailValidation() {
        ContentAuthMobileRegisterReq req = new ContentAuthMobileRegisterReq()
                .setMobile("13800138000")
                .setCode(null)
                .setNickname("testUser");

        Set<ConstraintViolation<ContentAuthMobileRegisterReq>> violations = validator.validate(req);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("验证码不能为空"));
    }

    @Test
    void registerByMobile_emptyCode_shouldFailValidation() {
        ContentAuthMobileRegisterReq req = new ContentAuthMobileRegisterReq()
                .setMobile("13800138000")
                .setCode("")
                .setNickname("testUser");

        Set<ConstraintViolation<ContentAuthMobileRegisterReq>> violations = validator.validate(req);

        assertThat(violations).isNotEmpty();
        // 空字符串触发 @NotBlank
        assertThat(violations).anyMatch(v -> v.getMessage().contains("验证码不能为空"));
    }

    // ========== 辅助方法 ==========

    private ContentAuthMobileRegisterReq buildValidReq() {
        return new ContentAuthMobileRegisterReq()
                .setMobile("13800138000")
                .setCode("123456")
                .setNickname("testUser");
    }
}
