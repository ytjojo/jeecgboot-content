package org.jeecg.modules.content.auth.biz;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.auth.dto.AuthLoginResult;
import org.jeecg.modules.content.auth.entity.ContentUserAccount;
import org.jeecg.modules.content.auth.entity.ContentUserCredential;
import org.jeecg.modules.content.auth.mapper.ContentUserAccountMapper;
import org.jeecg.modules.content.auth.mapper.ContentUserCredentialMapper;
import org.jeecg.modules.content.auth.req.ContentAuthEmailRegisterReq;
import org.jeecg.modules.content.auth.service.EmailSenderPort;
import org.jeecg.modules.content.auth.service.IContentTokenService;
import org.jeecg.modules.content.auth.service.LoginTokenGeneratorPort;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.gateway.SystemUserAccountGateway;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.req.account.ContentEmailRegisterReq;
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
 * 邮箱密码注册 + 邮箱确认流程测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentAuthBizServiceRegisterEmailTest {

    @Mock
    private ContentUserCredentialMapper credentialMapper;

    @Mock
    private ContentUserAccountMapper accountMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private SystemUserAccountGateway systemUserAccountGateway;

    @Mock
    private IContentTokenService tokenService;

    @Mock
    private EmailSenderPort emailSenderPort;

    @Mock
    private LoginTokenGeneratorPort loginTokenGeneratorPort;

    @Mock
    private ContentUserDeviceSessionMapper deviceSessionMapper;

    @InjectMocks
    private ContentAuthBizServiceImpl service;

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        // 初始化MyBatis-Plus的TableInfo，避免LambdaQueryWrapper在测试中报错
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                ContentUserCredential.class);
    }

    // ==================== 注册成功场景 ====================

    @Test
    void registerByEmail_validRequest_createsEverythingAndSendsEmail() {
        // given: 邮箱未注册
        when(credentialMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(systemUserAccountGateway.createUserByEmail(any(ContentEmailRegisterReq.class))).thenReturn("user-001");
        when(credentialMapper.insert(any(ContentUserCredential.class))).thenReturn(1);
        when(accountMapper.insert(any(ContentUserAccount.class))).thenReturn(1);
        when(profileMapper.insert(any(ContentUserProfile.class))).thenReturn(1);
        when(tokenService.generateEmailVerifyToken("user-001", "test@example.com")).thenReturn("verify-token-abc");
        when(emailSenderPort.send(eq("test@example.com"), anyString(), anyString())).thenReturn(true);
        when(loginTokenGeneratorPort.generateToken("user-001", "PC")).thenReturn("access-token-abc");
        when(deviceSessionMapper.insert(any(ContentUserDeviceSession.class))).thenReturn(1);

        ContentAuthEmailRegisterReq req = validReq();

        // when
        AuthLoginResult result = service.registerByEmail(req);

        // then
        assertThat(result.getUserId()).isEqualTo("user-001");
        assertThat(result.getAccessToken()).isEqualTo("access-token-abc");

        // 验证调用了gateway创建用户
        ArgumentCaptor<ContentEmailRegisterReq> gatewayReqCaptor = ArgumentCaptor.forClass(ContentEmailRegisterReq.class);
        verify(systemUserAccountGateway).createUserByEmail(gatewayReqCaptor.capture());
        assertThat(gatewayReqCaptor.getValue().getEmail()).isEqualTo("test@example.com");
        assertThat(gatewayReqCaptor.getValue().getPassword()).isEqualTo("Pass1234");
        assertThat(gatewayReqCaptor.getValue().getNickname()).isEqualTo("测试用户");

        // 验证创建了凭证记录
        ArgumentCaptor<ContentUserCredential> credCaptor = ArgumentCaptor.forClass(ContentUserCredential.class);
        verify(credentialMapper).insert(credCaptor.capture());
        assertThat(credCaptor.getValue().getUserId()).isEqualTo("user-001");
        assertThat(credCaptor.getValue().getCredentialType()).isEqualTo("EMAIL_CODE");
        assertThat(credCaptor.getValue().getCredentialValue()).isEqualTo("test@example.com");
        assertThat(credCaptor.getValue().getVerified()).isFalse();
        assertThat(credCaptor.getValue().getStatus()).isEqualTo("ACTIVE");

        // 验证创建了账号记录
        ArgumentCaptor<ContentUserAccount> accountCaptor = ArgumentCaptor.forClass(ContentUserAccount.class);
        verify(accountMapper).insert(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getUserId()).isEqualTo("user-001");
        assertThat(accountCaptor.getValue().getNickname()).isEqualTo("测试用户");
        assertThat(accountCaptor.getValue().getAccountStatus()).isEqualTo("ACTIVE");

        // 验证初始化了用户资料
        ArgumentCaptor<ContentUserProfile> profileCaptor = ArgumentCaptor.forClass(ContentUserProfile.class);
        verify(profileMapper).insert(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getUserId()).isEqualTo("user-001");
        assertThat(profileCaptor.getValue().getNickname()).isEqualTo("测试用户");

        // 验证发送了确认邮件
        verify(tokenService).generateEmailVerifyToken("user-001", "test@example.com");
        verify(emailSenderPort).send(eq("test@example.com"), eq("请确认您的邮箱"), contains("verify-token-abc"));
    }

    // ==================== 邮箱为空/格式错误 ====================

    @Test
    void registerByEmail_nullEmail_validationFails() {
        ContentAuthEmailRegisterReq req = validReq().setEmail(null);
        Set<ConstraintViolation<ContentAuthEmailRegisterReq>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void registerByEmail_emptyEmail_validationFails() {
        ContentAuthEmailRegisterReq req = validReq().setEmail("");
        Set<ConstraintViolation<ContentAuthEmailRegisterReq>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void registerByEmail_invalidEmailFormat_validationFails() {
        ContentAuthEmailRegisterReq req = validReq().setEmail("not-an-email");
        Set<ConstraintViolation<ContentAuthEmailRegisterReq>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    // ==================== 密码长度/强度校验 ====================

    @Test
    void registerByEmail_passwordTooShort_validationFails() {
        ContentAuthEmailRegisterReq req = validReq().setPassword("Ab1"); // 3位，少于8位
        Set<ConstraintViolation<ContentAuthEmailRegisterReq>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("password") && v.getMessage().contains("8"));
    }

    @Test
    void registerByEmail_passwordWithoutNumber_validationFails() {
        ContentAuthEmailRegisterReq req = validReq().setPassword("abcdefgh"); // 纯字母，无数字
        Set<ConstraintViolation<ContentAuthEmailRegisterReq>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("password") && v.getMessage().contains("字母和数字"));
    }

    // ==================== 重复邮箱 ====================

    @Test
    void registerByEmail_duplicateEmail_throwsException() {
        when(credentialMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        ContentAuthEmailRegisterReq req = validReq();

        assertThatThrownBy(() -> service.registerByEmail(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessage("该邮箱已被注册");

        // 不应调用gateway创建用户
        verify(systemUserAccountGateway, never()).createUserByEmail(any());
    }

    // ==================== 邮箱确认场景 ====================

    @Test
    void confirmEmail_validToken_returnsUserIdAndSetsVerified() {
        when(tokenService.validateAndConsumeToken("valid-token", "EMAIL_VERIFY")).thenReturn("user-001");
        ContentUserCredential existingCred = new ContentUserCredential()
                .setUserId("user-001")
                .setCredentialType("EMAIL_CODE")
                .setCredentialValue("test@example.com")
                .setVerified(false);
        existingCred.setId("cred-001");
        when(credentialMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingCred);
        doReturn(1).when(credentialMapper).updateById(any(ContentUserCredential.class));

        String userId = service.confirmEmail("valid-token");

        assertThat(userId).isEqualTo("user-001");

        // 验证凭证被更新为已验证
        ArgumentCaptor<ContentUserCredential> captor = ArgumentCaptor.forClass(ContentUserCredential.class);
        verify(credentialMapper).updateById((ContentUserCredential) captor.capture());
        assertThat(captor.getValue().getVerified()).isTrue();
        assertThat(captor.getValue().getVerifyTime()).isNotNull();
    }

    @Test
    void confirmEmail_expiredOrNullToken_throwsException() {
        when(tokenService.validateAndConsumeToken("expired-token", "EMAIL_VERIFY")).thenReturn(null);

        assertThatThrownBy(() -> service.confirmEmail("expired-token"))
                .isInstanceOf(JeecgBootException.class)
                .hasMessage("验证链接无效或已过期");

        verify(credentialMapper, never()).updateById(any(ContentUserCredential.class));
    }

    @Test
    void confirmEmail_alreadyUsedToken_throwsException() {
        // validateAndConsumeToken对已使用的token返回null
        when(tokenService.validateAndConsumeToken("used-token", "EMAIL_VERIFY")).thenReturn(null);

        assertThatThrownBy(() -> service.confirmEmail("used-token"))
                .isInstanceOf(JeecgBootException.class)
                .hasMessage("验证链接无效或已过期");
    }

    // ==================== 工具方法 ====================

    private ContentAuthEmailRegisterReq validReq() {
        return new ContentAuthEmailRegisterReq()
                .setEmail("test@example.com")
                .setPassword("Pass1234")
                .setNickname("测试用户");
    }
}
