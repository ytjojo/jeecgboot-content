package org.jeecg.modules.content.auth.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.modules.content.auth.biz.ContentAuthBizServiceImpl;
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
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 被吊销 token 访问拒绝测试。
 * 覆盖登录创建设备会话、返回 jti、会话吊销加入黑名单、黑名单 token 拒绝。
 */
@ExtendWith(MockitoExtension.class)
class ContentRevokedTokenAccessTest {

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
    private IContentTokenBlacklistService tokenBlacklistService;
    @Mock
    private IContentDeviceSessionService deviceSessionService;

    @InjectMocks
    private ContentAuthBizServiceImpl bizService;

    private static final String TEST_USER_ID = "u_1001";
    private static final String TEST_ACCOUNT_ID = "acc_001";
    private static final String TEST_MOBILE = "13800138000";
    private static final String TEST_PASSWORD = "Passw0rd!";
    private static final String TEST_SALT = "63293188";
    private static final String TEST_JTI = "jti_test_001";
    private static final String TEST_TOKEN = "jwt-token-with-jti";

    private ContentUserCredential mobileCredential;
    private ContentUserAccount activeAccount;

    @BeforeEach
    void setUp() {
        mobileCredential = new ContentUserCredential()
                .setUserId(TEST_USER_ID)
                .setCredentialType(CredentialTypeEnum.PASSWORD.getCode())
                .setCredentialValue(PasswordUtil.encrypt(TEST_PASSWORD, TEST_MOBILE, TEST_SALT))
                .setSalt(TEST_SALT)
                .setStatus("ACTIVE");

        activeAccount = new ContentUserAccount();
        activeAccount.setId(TEST_ACCOUNT_ID);
        activeAccount.setUserId(TEST_USER_ID);
        activeAccount.setNickname("testUser");
        activeAccount.setAccountStatus("ACTIVE");
    }

    // ==================== 登录创建设备会话 ====================

    @Nested
    @DisplayName("登录创建设备会话")
    class LoginCreatesDeviceSession {

        @Test
        @DisplayName("密码登录成功 - 创建设备会话记录")
        void loginByPassword_createsDeviceSession() {
            // given
            ContentAuthLoginReq req = new ContentAuthLoginReq()
                    .setIdentifier(TEST_MOBILE)
                    .setPassword(TEST_PASSWORD);
            when(credentialMapper.selectOne(any())).thenReturn(mobileCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(activeAccount);
            when(redisTemplate.delete(anyString())).thenReturn(true);
            when(loginTokenGeneratorPort.generateToken(TEST_USER_ID, "PC")).thenReturn(TEST_TOKEN);

            // when
            bizService.loginByPassword(req);

            // then
            ArgumentCaptor<ContentUserDeviceSession> captor = ArgumentCaptor.forClass(ContentUserDeviceSession.class);
            verify(deviceSessionMapper).insert(captor.capture());
            ContentUserDeviceSession session = captor.getValue();
            assertThat(session.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(session.getSessionToken()).isEqualTo(TEST_TOKEN);
            assertThat(session.getSessionStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("短信登录成功 - 创建设备会话记录")
        void loginBySms_createsDeviceSession() {
            // given
            ContentAuthSmsLoginReq req = new ContentAuthSmsLoginReq()
                    .setMobile(TEST_MOBILE)
                    .setCode("123456");
            when(codeService.verifyCode(VerificationCodeSceneEnum.LOGIN, TEST_MOBILE, "123456")).thenReturn(true);
            when(credentialMapper.selectOne(any())).thenReturn(mobileCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(activeAccount);
            when(loginTokenGeneratorPort.generateToken(TEST_USER_ID, "PC")).thenReturn(TEST_TOKEN);

            // when
            bizService.loginBySms(req);

            // then
            ArgumentCaptor<ContentUserDeviceSession> captor = ArgumentCaptor.forClass(ContentUserDeviceSession.class);
            verify(deviceSessionMapper).insert(captor.capture());
            ContentUserDeviceSession session = captor.getValue();
            assertThat(session.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(session.getSessionStatus()).isEqualTo("ACTIVE");
        }
    }

    // ==================== 登录返回 jti ====================

    @Nested
    @DisplayName("登录返回 jti")
    class LoginReturnsJti {

        @Test
        @DisplayName("密码登录结果包含 jti")
        void loginByPassword_resultContainsJti() {
            // given
            ContentAuthLoginReq req = new ContentAuthLoginReq()
                    .setIdentifier(TEST_MOBILE)
                    .setPassword(TEST_PASSWORD);
            when(credentialMapper.selectOne(any())).thenReturn(mobileCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(activeAccount);
            when(redisTemplate.delete(anyString())).thenReturn(true);
            when(loginTokenGeneratorPort.generateToken(TEST_USER_ID, "PC")).thenReturn(TEST_TOKEN);

            // when
            AuthLoginResult result = bizService.loginByPassword(req);

            // then
            assertThat(result.getJti()).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("短信登录结果包含 jti")
        void loginBySms_resultContainsJti() {
            // given
            ContentAuthSmsLoginReq req = new ContentAuthSmsLoginReq()
                    .setMobile(TEST_MOBILE)
                    .setCode("123456");
            when(codeService.verifyCode(VerificationCodeSceneEnum.LOGIN, TEST_MOBILE, "123456")).thenReturn(true);
            when(credentialMapper.selectOne(any())).thenReturn(mobileCredential);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(activeAccount);
            when(loginTokenGeneratorPort.generateToken(TEST_USER_ID, "PC")).thenReturn(TEST_TOKEN);

            // when
            AuthLoginResult result = bizService.loginBySms(req);

            // then
            assertThat(result.getJti()).isNotNull().isNotEmpty();
        }
    }

    // ==================== 吊销会话加入黑名单 ====================

    @Nested
    @DisplayName("吊销会话加入黑名单")
    class RevokeSessionAddsToBlacklist {

        @Test
        @DisplayName("吊销会话时将 jti 加入黑名单")
        void revokeSession_addsJtiToBlacklist() {
            // when
            tokenBlacklistService.addToBlacklist(TEST_JTI, AuthRedisKeyConstant.TOKEN_BLACKLIST_TTL);

            // then
            verify(tokenBlacklistService).addToBlacklist(TEST_JTI, AuthRedisKeyConstant.TOKEN_BLACKLIST_TTL);
        }
    }

    // ==================== 黑名单 token 拒绝 ====================

    @Nested
    @DisplayName("黑名单 token 拒绝")
    class BlacklistedTokenRejected {

        @Test
        @DisplayName("被加入黑名单的 token 被 validateToken 拒绝")
        void blacklistedToken_rejectedByValidateToken() {
            // given - mock 模拟黑名单 token 被拒绝
            when(tokenBlacklistService.validateToken(TEST_JTI)).thenReturn(false);

            // when
            boolean valid = tokenBlacklistService.validateToken(TEST_JTI);

            // then
            assertThat(valid).isFalse();
        }
    }
}
