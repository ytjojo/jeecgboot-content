package org.jeecg.modules.content.auth.biz;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.modules.content.auth.constant.AuthRedisKeyConstant;
import org.jeecg.modules.content.auth.dto.AuthLoginResult;
import org.jeecg.modules.content.auth.entity.ContentUserAccount;
import org.jeecg.modules.content.auth.entity.ContentUserCredential;
import org.jeecg.modules.content.auth.enums.CredentialTypeEnum;
import org.jeecg.modules.content.auth.enums.VerificationCodeSceneEnum;
import org.jeecg.modules.content.auth.mapper.ContentUserAccountMapper;
import org.jeecg.modules.content.auth.mapper.ContentUserCredentialMapper;
import org.jeecg.modules.content.auth.req.ContentAuthLoginReq;
import org.jeecg.modules.content.auth.req.ContentAuthSmsLoginReq;
import org.jeecg.modules.content.auth.service.CaptchaVerifyPort;
import org.jeecg.modules.content.auth.service.EmailSenderPort;
import org.jeecg.modules.content.auth.service.IContentTokenService;
import org.jeecg.modules.content.auth.service.IContentVerificationCodeService;
import org.jeecg.modules.content.auth.service.LoginTokenGeneratorPort;
import org.jeecg.modules.content.auth.service.SmsSenderPort;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 登录业务逻辑测试。
 * 覆盖密码登录和短信验证码登录的核心场景。
 */
@ExtendWith(MockitoExtension.class)
class ContentAuthBizServiceLoginTest {

    @Mock
    private ContentUserCredentialMapper credentialMapper;
    @Mock
    private ContentUserAccountMapper accountMapper;
    @Mock
    private IContentVerificationCodeService codeService;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private LoginTokenGeneratorPort loginTokenGeneratorPort;
    @Mock
    private ContentUserDeviceSessionMapper deviceSessionMapper;
    @Mock
    private SmsSenderPort smsSenderPort;
    @Mock
    private CaptchaVerifyPort captchaVerifyPort;
    @Mock
    private EmailSenderPort emailSenderPort;
    @Mock
    private IContentTokenService tokenService;

    @InjectMocks
    private ContentAuthBizServiceImpl bizService;

    private static final String TEST_USER_ID = "u_1001";
    private static final String TEST_ACCOUNT_ID = "acc_001";
    private static final String TEST_MOBILE = "13800138000";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Passw0rd!";
    private static final String TEST_SALT = "63293188";

    private ContentUserCredential mobileCredential;
    private ContentUserCredential emailCredential;
    private ContentUserAccount activeAccount;

    @BeforeEach
    void setUp() {
        mobileCredential = new ContentUserCredential()
                .setUserId(TEST_USER_ID)
                .setCredentialType(CredentialTypeEnum.PASSWORD.getCode())
                .setCredentialValue(PasswordUtil.encrypt(TEST_PASSWORD, TEST_MOBILE, TEST_SALT))
                .setSalt(TEST_SALT)
                .setStatus("ACTIVE");

        emailCredential = new ContentUserCredential()
                .setUserId(TEST_USER_ID)
                .setCredentialType(CredentialTypeEnum.PASSWORD.getCode())
                .setCredentialValue(PasswordUtil.encrypt(TEST_PASSWORD, TEST_EMAIL, TEST_SALT))
                .setSalt(TEST_SALT)
                .setStatus("ACTIVE");

        activeAccount = new ContentUserAccount();
        activeAccount.setId(TEST_ACCOUNT_ID);
        activeAccount.setUserId(TEST_USER_ID);
        activeAccount.setNickname("testUser");
        activeAccount.setAccountStatus("ACTIVE");
    }

    // ==================== 密码登录测试 ====================

    @Nested
    @DisplayName("密码登录")
    class LoginByPassword {

        @Test
        @DisplayName("手机号+密码登录成功 - 返回用户ID和token")
        void loginByPassword_mobile_success() {
            // given
            ContentAuthLoginReq req = new ContentAuthLoginReq()
                    .setIdentifier(TEST_MOBILE)
                    .setPassword(TEST_PASSWORD);
            when(credentialMapper.selectOne(any())).thenReturn(mobileCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(activeAccount);
            when(redisTemplate.delete(anyString())).thenReturn(true);
            when(loginTokenGeneratorPort.generateToken(TEST_USER_ID, "PC")).thenReturn("mock-jwt-token");

            // when
            AuthLoginResult result = bizService.loginByPassword(req);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(result.getAccessToken()).isEqualTo("mock-jwt-token");
            assertThat(result.getTokenType()).isEqualTo("Bearer");
            verify(accountMapper).updateById(any(ContentUserAccount.class));
        }

        @Test
        @DisplayName("邮箱+密码登录成功")
        void loginByPassword_email_success() {
            // given
            ContentAuthLoginReq req = new ContentAuthLoginReq()
                    .setIdentifier(TEST_EMAIL)
                    .setPassword(TEST_PASSWORD);
            when(credentialMapper.selectOne(any())).thenReturn(emailCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(activeAccount);
            when(redisTemplate.delete(anyString())).thenReturn(true);
            when(loginTokenGeneratorPort.generateToken(TEST_USER_ID, "PC")).thenReturn("mock-jwt-token");

            // when
            AuthLoginResult result = bizService.loginByPassword(req);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("未知账号 - 抛出通用错误信息（不暴露账号是否存在）")
        void loginByPassword_unknownIdentifier_throwsGenericError() {
            // given
            ContentAuthLoginReq req = new ContentAuthLoginReq()
                    .setIdentifier("13900000000")
                    .setPassword(TEST_PASSWORD);
            when(credentialMapper.selectOne(any())).thenReturn(null);

            // when / then
            assertThatThrownBy(() -> bizService.loginByPassword(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("账号或密码错误");
        }

        @Test
        @DisplayName("密码错误 - 抛出通用错误信息（不暴露是密码错误）")
        void loginByPassword_wrongPassword_throwsGenericError() {
            // given
            ContentAuthLoginReq req = new ContentAuthLoginReq()
                    .setIdentifier(TEST_MOBILE)
                    .setPassword("WrongPassword!");
            when(credentialMapper.selectOne(any())).thenReturn(mobileCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(activeAccount);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.increment(anyString())).thenReturn(1L);

            // when / then
            assertThatThrownBy(() -> bizService.loginByPassword(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("账号或密码错误");
        }

        @Test
        @DisplayName("账号已锁定 - 抛出锁定提示")
        void loginByPassword_accountLocked_throwsLockedError() {
            // given
            ContentAuthLoginReq req = new ContentAuthLoginReq()
                    .setIdentifier(TEST_MOBILE)
                    .setPassword(TEST_PASSWORD);
            ContentUserAccount lockedAccount = new ContentUserAccount();
            lockedAccount.setId(TEST_ACCOUNT_ID);
            lockedAccount.setUserId(TEST_USER_ID);
            lockedAccount.setAccountStatus("ACTIVE");
            lockedAccount.setLockedUntil(new Date(System.currentTimeMillis() + 600_000));
            when(credentialMapper.selectOne(any())).thenReturn(mobileCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(lockedAccount);

            // when / then
            assertThatThrownBy(() -> bizService.loginByPassword(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("账号已锁定，请稍后再试");
        }

        @Test
        @DisplayName("账号已注销 - 抛出注销提示")
        void loginByPassword_accountCancelled_throwsCancelledError() {
            // given
            ContentAuthLoginReq req = new ContentAuthLoginReq()
                    .setIdentifier(TEST_MOBILE)
                    .setPassword(TEST_PASSWORD);
            // selectActiveByUserId只查ACTIVE状态，已注销账号返回null
            when(credentialMapper.selectOne(any())).thenReturn(mobileCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(null);

            // when / then
            assertThatThrownBy(() -> bizService.loginByPassword(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("账号已注销");
        }

        @Test
        @DisplayName("连续密码错误10次 - 触发验证码挑战")
        void loginByPassword_failCount10_triggersCaptcha() {
            // given
            ContentAuthLoginReq req = new ContentAuthLoginReq()
                    .setIdentifier(TEST_MOBILE)
                    .setPassword("WrongPassword!");
            when(credentialMapper.selectOne(any())).thenReturn(mobileCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(activeAccount);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.increment(anyString())).thenReturn(10L);

            // when / then
            assertThatThrownBy(() -> bizService.loginByPassword(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessageContaining("验证码");
        }

        @Test
        @DisplayName("连续密码错误20次 - 锁定账号30分钟")
        void loginByPassword_failCount20_locksAccount() {
            // given
            ContentAuthLoginReq req = new ContentAuthLoginReq()
                    .setIdentifier(TEST_MOBILE)
                    .setPassword("WrongPassword!");
            when(credentialMapper.selectOne(any())).thenReturn(mobileCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(activeAccount);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.increment(anyString())).thenReturn(20L);

            // when / then
            assertThatThrownBy(() -> bizService.loginByPassword(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("账号已锁定，请30分钟后再试");
            verify(accountMapper).updateById(any(ContentUserAccount.class));
        }

        @Test
        @DisplayName("登录成功后清除密码错误计数")
        void loginByPassword_success_clearsFailCount() {
            // given
            ContentAuthLoginReq req = new ContentAuthLoginReq()
                    .setIdentifier(TEST_MOBILE)
                    .setPassword(TEST_PASSWORD);
            when(credentialMapper.selectOne(any())).thenReturn(mobileCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(activeAccount);
            when(redisTemplate.delete(anyString())).thenReturn(true);
            when(loginTokenGeneratorPort.generateToken(TEST_USER_ID, "PC")).thenReturn("mock-jwt-token");

            // when
            bizService.loginByPassword(req);

            // then
            verify(redisTemplate).delete(AuthRedisKeyConstant.PWD_FAIL_PREFIX + TEST_ACCOUNT_ID);
        }
    }

    // ==================== 短信验证码登录测试 ====================

    @Nested
    @DisplayName("短信验证码登录")
    class LoginBySms {

        @Test
        @DisplayName("验证码正确且账号存在 - 登录成功")
        void loginBySms_success() {
            // given
            ContentAuthSmsLoginReq req = new ContentAuthSmsLoginReq()
                    .setMobile(TEST_MOBILE)
                    .setCode("123456");
            when(codeService.verifyCode(VerificationCodeSceneEnum.LOGIN, TEST_MOBILE, "123456")).thenReturn(true);
            when(credentialMapper.selectOne(any())).thenReturn(mobileCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(activeAccount);
            when(loginTokenGeneratorPort.generateToken(TEST_USER_ID, "PC")).thenReturn("mock-jwt-token");

            // when
            AuthLoginResult result = bizService.loginBySms(req);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(result.getAccessToken()).isEqualTo("mock-jwt-token");
            verify(accountMapper).updateById(any(ContentUserAccount.class));
        }

        @Test
        @DisplayName("验证码无效 - 抛出验证码错误")
        void loginBySms_invalidCode_throwsError() {
            // given
            ContentAuthSmsLoginReq req = new ContentAuthSmsLoginReq()
                    .setMobile(TEST_MOBILE)
                    .setCode("000000");
            when(codeService.verifyCode(VerificationCodeSceneEnum.LOGIN, TEST_MOBILE, "000000")).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> bizService.loginBySms(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("验证码错误或已过期");
        }

        @Test
        @DisplayName("手机号未注册 - 抛出未注册提示")
        void loginBySms_unknownMobile_throwsError() {
            // given
            ContentAuthSmsLoginReq req = new ContentAuthSmsLoginReq()
                    .setMobile("13900000000")
                    .setCode("123456");
            when(codeService.verifyCode(VerificationCodeSceneEnum.LOGIN, "13900000000", "123456")).thenReturn(true);
            when(credentialMapper.selectOne(any())).thenReturn(null);

            // when / then
            assertThatThrownBy(() -> bizService.loginBySms(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("该手机号未注册");
        }

        @Test
        @DisplayName("账号已注销 - 抛出注销提示")
        void loginBySms_accountCancelled_throwsError() {
            // given
            ContentAuthSmsLoginReq req = new ContentAuthSmsLoginReq()
                    .setMobile(TEST_MOBILE)
                    .setCode("123456");
            // selectActiveByUserId只查ACTIVE状态，已注销账号返回null
            when(codeService.verifyCode(VerificationCodeSceneEnum.LOGIN, TEST_MOBILE, "123456")).thenReturn(true);
            when(credentialMapper.selectOne(any())).thenReturn(mobileCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(null);

            // when / then
            assertThatThrownBy(() -> bizService.loginBySms(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("账号已注销");
        }
    }

    // ==================== 发送短信验证码测试 ====================

    @Nested
    @DisplayName("sendSmsCode - 发送短信验证码")
    class SendSmsCode {

        @Test
        @DisplayName("发送成功 - 无图形验证码")
        void sendSmsCode_success_noCaptcha() {
            when(codeService.isInCooldown(VerificationCodeSceneEnum.LOGIN, TEST_MOBILE)).thenReturn(false);
            when(codeService.generateCode(VerificationCodeSceneEnum.LOGIN, TEST_MOBILE)).thenReturn("123456");
            when(smsSenderPort.send(eq(TEST_MOBILE), anyString())).thenReturn(true);

            bizService.sendSmsCode(TEST_MOBILE, null, null, null);

            verify(smsSenderPort).send(eq(TEST_MOBILE), contains("123456"));
        }

        @Test
        @DisplayName("发送成功 - 图形验证码通过")
        void sendSmsCode_success_withCaptcha() {
            when(captchaVerifyPort.verify("captcha_code", null)).thenReturn(true);
            when(codeService.isInCooldown(VerificationCodeSceneEnum.LOGIN, TEST_MOBILE)).thenReturn(false);
            when(codeService.generateCode(VerificationCodeSceneEnum.LOGIN, TEST_MOBILE)).thenReturn("654321");
            when(smsSenderPort.send(eq(TEST_MOBILE), anyString())).thenReturn(true);

            bizService.sendSmsCode(TEST_MOBILE, null, "captcha_id", "captcha_code");

            verify(captchaVerifyPort).verify("captcha_code", null);
            verify(smsSenderPort).send(eq(TEST_MOBILE), contains("654321"));
        }

        @Test
        @DisplayName("图形验证码错误 - 抛出异常")
        void sendSmsCode_invalidCaptcha_throws() {
            when(captchaVerifyPort.verify("wrong_code", null)).thenReturn(false);

            assertThatThrownBy(() -> bizService.sendSmsCode(TEST_MOBILE, null, "captcha_id", "wrong_code"))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("图形验证码错误");
        }

        @Test
        @DisplayName("冷却期内 - 抛出频率限制异常")
        void sendSmsCode_inCooldown_throws() {
            when(codeService.isInCooldown(VerificationCodeSceneEnum.LOGIN, TEST_MOBILE)).thenReturn(true);

            assertThatThrownBy(() -> bizService.sendSmsCode(TEST_MOBILE, null, null, null))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("验证码发送过于频繁，请稍后再试");
        }

        @Test
        @DisplayName("短信发送失败 - 抛出异常")
        void sendSmsCode_sendFailed_throws() {
            when(codeService.isInCooldown(VerificationCodeSceneEnum.LOGIN, TEST_MOBILE)).thenReturn(false);
            when(codeService.generateCode(VerificationCodeSceneEnum.LOGIN, TEST_MOBILE)).thenReturn("123456");
            when(smsSenderPort.send(eq(TEST_MOBILE), anyString())).thenReturn(false);

            assertThatThrownBy(() -> bizService.sendSmsCode(TEST_MOBILE, null, null, null))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("短信发送失败，请稍后再试");
        }
    }

    // ==================== 发送邮箱验证码测试 ====================

    @Nested
    @DisplayName("sendEmailCode - 发送邮箱验证码")
    class SendEmailCode {

        @Test
        @DisplayName("发送成功 - 无图形验证码")
        void sendEmailCode_success_noCaptcha() {
            when(codeService.isInCooldown(VerificationCodeSceneEnum.BIND_EMAIL, TEST_EMAIL)).thenReturn(false);
            when(codeService.generateCode(VerificationCodeSceneEnum.BIND_EMAIL, TEST_EMAIL)).thenReturn("123456");
            when(emailSenderPort.send(eq(TEST_EMAIL), anyString(), anyString())).thenReturn(true);

            bizService.sendEmailCode(TEST_EMAIL, null, null);

            verify(emailSenderPort).send(eq(TEST_EMAIL), eq("内容社区验证码"), contains("123456"));
        }

        @Test
        @DisplayName("图形验证码错误 - 抛出异常")
        void sendEmailCode_invalidCaptcha_throws() {
            when(captchaVerifyPort.verify("wrong", null)).thenReturn(false);

            assertThatThrownBy(() -> bizService.sendEmailCode(TEST_EMAIL, "captcha_id", "wrong"))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("图形验证码错误");
        }

        @Test
        @DisplayName("冷却期内 - 抛出频率限制异常")
        void sendEmailCode_inCooldown_throws() {
            when(codeService.isInCooldown(VerificationCodeSceneEnum.BIND_EMAIL, TEST_EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> bizService.sendEmailCode(TEST_EMAIL, null, null))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("验证码发送过于频繁，请稍后再试");
        }

        @Test
        @DisplayName("邮件发送失败 - 抛出异常")
        void sendEmailCode_sendFailed_throws() {
            when(codeService.isInCooldown(VerificationCodeSceneEnum.BIND_EMAIL, TEST_EMAIL)).thenReturn(false);
            when(codeService.generateCode(VerificationCodeSceneEnum.BIND_EMAIL, TEST_EMAIL)).thenReturn("123456");
            when(emailSenderPort.send(eq(TEST_EMAIL), anyString(), anyString())).thenReturn(false);

            assertThatThrownBy(() -> bizService.sendEmailCode(TEST_EMAIL, null, null))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("邮件发送失败，请稍后再试");
        }
    }

    // ==================== 刷新Token测试 ====================

    @Nested
    @DisplayName("refreshToken - 刷新Token")
    class RefreshToken {

        @Test
        @DisplayName("刷新成功 - 返回新token")
        void refreshToken_success() {
            when(tokenService.validateAndConsumeToken("valid_refresh_token", "REFRESH_TOKEN")).thenReturn(TEST_USER_ID);
            when(loginTokenGeneratorPort.generateToken(TEST_USER_ID, "PC")).thenReturn("new-access-token");

            AuthLoginResult result = bizService.refreshToken("valid_refresh_token");

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(result.getAccessToken()).isEqualTo("new-access-token");
            assertThat(result.getTokenType()).isEqualTo("Bearer");
            verify(deviceSessionMapper).insert(any(ContentUserDeviceSession.class));
        }

        @Test
        @DisplayName("refreshToken无效 - 抛出异常")
        void refreshToken_invalid_throws() {
            when(tokenService.validateAndConsumeToken("invalid_token", "REFRESH_TOKEN")).thenReturn(null);

            assertThatThrownBy(() -> bizService.refreshToken("invalid_token"))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("refreshToken 无效或已过期");
        }
    }

    // ==================== 登出测试 ====================

    @Nested
    @DisplayName("logout - 用户登出")
    class Logout {

        @Test
        @DisplayName("登出成功 - 清除Redis会话")
        void logout_success() {
            when(redisTemplate.delete(AuthRedisKeyConstant.TOKEN_BLACKLIST_PREFIX + TEST_USER_ID)).thenReturn(true);

            bizService.logout(TEST_USER_ID);

            verify(redisTemplate).delete(AuthRedisKeyConstant.TOKEN_BLACKLIST_PREFIX + TEST_USER_ID);
        }
    }

    // ==================== 获取验证码图片测试 ====================

    @Nested
    @DisplayName("getCaptchaImage - 获取验证码图片")
    class GetCaptchaImage {

        @Test
        @DisplayName("返回captchaId和imageBase64")
        void getCaptchaImage_returnsMap() {
            Map<String, String> result = bizService.getCaptchaImage();

            assertThat(result).isNotNull();
            assertThat(result.get("captchaId")).isNotNull().isNotEmpty();
            assertThat(result.get("imageBase64")).isEqualTo("");
        }
    }

    // ==================== 验证验证码测试 ====================

    @Nested
    @DisplayName("verifyCaptcha - 验证验证码")
    class VerifyCaptcha {

        @Test
        @DisplayName("验证通过 - 返回true")
        void verifyCaptcha_valid_returnsTrue() {
            when(captchaVerifyPort.verify("code", null)).thenReturn(true);

            assertThat(bizService.verifyCaptcha("id", "code")).isTrue();
        }

        @Test
        @DisplayName("验证失败 - 返回false")
        void verifyCaptcha_invalid_returnsFalse() {
            when(captchaVerifyPort.verify("wrong", null)).thenReturn(false);

            assertThat(bizService.verifyCaptcha("id", "wrong")).isFalse();
        }
    }

    // ==================== 锁定状态查询测试 ====================

    @Nested
    @DisplayName("getLockStatus - 锁定状态查询")
    class GetLockStatus {

        @Test
        @DisplayName("未锁定 - 返回locked=false")
        void getLockStatus_notLocked() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(AuthRedisKeyConstant.PWD_FAIL_PREFIX + TEST_MOBILE)).thenReturn("3");

            Map<String, Object> status = bizService.getLockStatus(TEST_MOBILE);

            assertThat(status.get("locked")).isEqualTo(false);
            assertThat(status.get("attempts")).isEqualTo(3);
            assertThat(status.get("maxAttempts")).isEqualTo(AuthRedisKeyConstant.LOGIN_FAIL_LOCK_THRESHOLD);
        }

        @Test
        @DisplayName("已锁定 - 返回locked=true")
        void getLockStatus_locked() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(AuthRedisKeyConstant.PWD_FAIL_PREFIX + TEST_MOBILE)).thenReturn("20");

            Map<String, Object> status = bizService.getLockStatus(TEST_MOBILE);

            assertThat(status.get("locked")).isEqualTo(true);
            assertThat(status.get("attempts")).isEqualTo(20);
        }

        @Test
        @DisplayName("无失败记录 - 返回attempts=0")
        void getLockStatus_noRecord() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(AuthRedisKeyConstant.PWD_FAIL_PREFIX + TEST_MOBILE)).thenReturn(null);

            Map<String, Object> status = bizService.getLockStatus(TEST_MOBILE);

            assertThat(status.get("locked")).isEqualTo(false);
            assertThat(status.get("attempts")).isEqualTo(0);
        }
    }
}
