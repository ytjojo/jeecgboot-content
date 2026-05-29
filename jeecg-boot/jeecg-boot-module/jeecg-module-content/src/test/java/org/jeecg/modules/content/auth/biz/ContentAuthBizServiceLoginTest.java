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
import org.jeecg.modules.content.auth.service.IContentVerificationCodeService;
import org.jeecg.modules.content.auth.service.LoginTokenGeneratorPort;
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
}
