package org.jeecg.modules.content.auth.biz;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.modules.content.auth.entity.ContentUserCredential;
import org.jeecg.modules.content.auth.entity.ContentUserPasswordHistory;
import org.jeecg.modules.content.auth.enums.CredentialTypeEnum;
import org.jeecg.modules.content.auth.enums.VerificationCodeSceneEnum;
import org.jeecg.modules.content.auth.mapper.ContentUserCredentialMapper;
import org.jeecg.modules.content.auth.mapper.ContentUserPasswordHistoryMapper;
import org.jeecg.modules.content.auth.req.ContentAuthResetPasswordByEmailReq;
import org.jeecg.modules.content.auth.req.ContentAuthResetPasswordByMobileReq;
import org.jeecg.modules.content.auth.service.IContentTokenService;
import org.jeecg.modules.content.auth.service.IContentVerificationCodeService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 密码重置业务逻辑测试。
 * 覆盖手机号重置和邮箱重置的核心场景，以及密码历史检查。
 */
@ExtendWith(MockitoExtension.class)
class ContentAuthBizServicePasswordResetTest {

    @Mock
    private IContentVerificationCodeService codeService;
    @Mock
    private ContentUserCredentialMapper credentialMapper;
    @Mock
    private ContentUserPasswordHistoryMapper passwordHistoryMapper;
    @Mock
    private IContentTokenService tokenService;

    @InjectMocks
    private ContentAuthBizServiceImpl bizService;

    private static final String TEST_USER_ID = "u_1001";
    private static final String TEST_MOBILE = "13800138000";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_OLD_PASSWORD = "OldPassw0rd";
    private static final String TEST_NEW_PASSWORD = "NewPassw0rd1";
    private static final String TEST_SALT = "63293188";

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private ContentUserCredential smsCredential;
    private ContentUserCredential emailCredential;
    private ContentUserCredential passwordCredential;

    @BeforeEach
    void setUp() {
        // SMS凭证：用于通过手机号找到userId
        smsCredential = new ContentUserCredential()
                .setUserId(TEST_USER_ID)
                .setCredentialType(CredentialTypeEnum.SMS_CODE.getCode())
                .setCredentialValue(TEST_MOBILE)
                .setStatus("ACTIVE");

        // 邮箱凭证：用于通过邮箱找到userId
        emailCredential = new ContentUserCredential()
                .setUserId(TEST_USER_ID)
                .setCredentialType(CredentialTypeEnum.EMAIL_CODE.getCode())
                .setCredentialValue(TEST_EMAIL)
                .setStatus("ACTIVE");

        // 密码凭证：存储当前密码
        passwordCredential = new ContentUserCredential()
                .setUserId(TEST_USER_ID)
                .setCredentialType(CredentialTypeEnum.PASSWORD.getCode())
                .setCredentialValue(PasswordUtil.encrypt(TEST_OLD_PASSWORD, TEST_MOBILE, TEST_SALT))
                .setSalt(TEST_SALT)
                .setStatus("ACTIVE");
    }

    // ==================== 手机号重置密码测试 ====================

    @Nested
    @DisplayName("手机号重置密码")
    class ResetPasswordByMobile {

        @Test
        @DisplayName("重置成功 - 验证码有效且密码未在历史中")
        void resetPasswordByMobile_success() {
            // given
            ContentAuthResetPasswordByMobileReq req = new ContentAuthResetPasswordByMobileReq()
                    .setMobile(TEST_MOBILE)
                    .setCode("123456")
                    .setNewPassword(TEST_NEW_PASSWORD);

            when(codeService.verifyCode(VerificationCodeSceneEnum.RESET_PASSWORD, TEST_MOBILE, "123456"))
                    .thenReturn(true);
            // 第一次selectOne查找SMS凭证，第二次查找密码凭证
            when(credentialMapper.selectOne(any()))
                    .thenReturn(smsCredential)
                    .thenReturn(passwordCredential);
            // 密码历史为空
            when(passwordHistoryMapper.selectList(any())).thenReturn(Collections.emptyList());

            // when
            bizService.resetPasswordByMobile(req);

            // then
            verify(codeService).verifyCode(VerificationCodeSceneEnum.RESET_PASSWORD, TEST_MOBILE, "123456");
            verify(credentialMapper, times(2)).selectOne(any());
            verify(credentialMapper).updateById(any(ContentUserCredential.class));
            // 旧密码和新密码都应加入历史
            verify(passwordHistoryMapper, times(2)).insert(any(ContentUserPasswordHistory.class));
        }

        @Test
        @DisplayName("验证码无效 - 抛出异常")
        void resetPasswordByMobile_invalidCode_throwsError() {
            // given
            ContentAuthResetPasswordByMobileReq req = new ContentAuthResetPasswordByMobileReq()
                    .setMobile(TEST_MOBILE)
                    .setCode("000000")
                    .setNewPassword(TEST_NEW_PASSWORD);

            when(codeService.verifyCode(VerificationCodeSceneEnum.RESET_PASSWORD, TEST_MOBILE, "000000"))
                    .thenReturn(false);

            // when / then
            assertThatThrownBy(() -> bizService.resetPasswordByMobile(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("验证码无效或已过期");
        }

        @Test
        @DisplayName("手机号未注册 - 抛出异常")
        void resetPasswordByMobile_unregisteredMobile_throwsError() {
            // given
            ContentAuthResetPasswordByMobileReq req = new ContentAuthResetPasswordByMobileReq()
                    .setMobile("13900000000")
                    .setCode("123456")
                    .setNewPassword(TEST_NEW_PASSWORD);

            when(codeService.verifyCode(VerificationCodeSceneEnum.RESET_PASSWORD, "13900000000", "123456"))
                    .thenReturn(true);
            when(credentialMapper.selectOne(any())).thenReturn(null);

            // when / then
            assertThatThrownBy(() -> bizService.resetPasswordByMobile(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("该手机号未注册");
        }

        @Test
        @DisplayName("密码不含数字 - 校验失败")
        void resetPasswordByMobile_weakPasswordNoDigit_validationFails() {
            // given
            ContentAuthResetPasswordByMobileReq req = new ContentAuthResetPasswordByMobileReq()
                    .setMobile(TEST_MOBILE)
                    .setCode("123456")
                    .setNewPassword("abcdefgh"); // 无数字

            // when
            Set<ConstraintViolation<ContentAuthResetPasswordByMobileReq>> violations = validator.validate(req);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getMessage().contains("字母和数字"));
        }

        @Test
        @DisplayName("密码长度不足 - 校验失败")
        void resetPasswordByMobile_passwordTooShort_validationFails() {
            // given
            ContentAuthResetPasswordByMobileReq req = new ContentAuthResetPasswordByMobileReq()
                    .setMobile(TEST_MOBILE)
                    .setCode("123456")
                    .setNewPassword("Ab1"); // 太短

            // when
            Set<ConstraintViolation<ContentAuthResetPasswordByMobileReq>> violations = validator.validate(req);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getMessage().contains("8到32位"));
        }

        @Test
        @DisplayName("复用最近3次密码 - 抛出不能使用历史密码异常")
        void resetPasswordByMobile_reuseLast3Passwords_throwsError() {
            // given
            ContentAuthResetPasswordByMobileReq req = new ContentAuthResetPasswordByMobileReq()
                    .setMobile(TEST_MOBILE)
                    .setCode("123456")
                    .setNewPassword(TEST_NEW_PASSWORD);

            when(codeService.verifyCode(VerificationCodeSceneEnum.RESET_PASSWORD, TEST_MOBILE, "123456"))
                    .thenReturn(true);
            // 第一次selectOne查找SMS凭证，第二次查找密码凭证
            when(credentialMapper.selectOne(any()))
                    .thenReturn(smsCredential)
                    .thenReturn(passwordCredential);

            // 构造3条历史记录，其中一条的密码哈希与新密码加密后相同
            // 盐值必须为8字节（PBEWithMD5AndDES要求）
            String reusedSalt = "hist0001";
            String reusedHash = PasswordUtil.encrypt(TEST_NEW_PASSWORD, TEST_MOBILE, reusedSalt);
            ContentUserPasswordHistory history1 = new ContentUserPasswordHistory()
                    .setUserId(TEST_USER_ID)
                    .setPasswordHash(reusedHash)
                    .setSalt(reusedSalt);
            ContentUserPasswordHistory history2 = new ContentUserPasswordHistory()
                    .setUserId(TEST_USER_ID)
                    .setPasswordHash(PasswordUtil.encrypt("OtherPass1", TEST_MOBILE, "hist0002"))
                    .setSalt("hist0002");
            ContentUserPasswordHistory history3 = new ContentUserPasswordHistory()
                    .setUserId(TEST_USER_ID)
                    .setPasswordHash(PasswordUtil.encrypt("OtherPass2", TEST_MOBILE, "hist0003"))
                    .setSalt("hist0003");

            when(passwordHistoryMapper.selectList(any()))
                    .thenReturn(Arrays.asList(history1, history2, history3));

            // when / then
            assertThatThrownBy(() -> bizService.resetPasswordByMobile(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("不能使用最近3次使用过的密码");
        }
    }

    // ==================== 邮箱重置密码测试 ====================

    @Nested
    @DisplayName("邮箱重置密码")
    class ResetPasswordByEmail {

        @Test
        @DisplayName("重置成功 - token有效")
        void resetPasswordByEmail_success() {
            // given
            ContentAuthResetPasswordByEmailReq req = new ContentAuthResetPasswordByEmailReq()
                    .setToken("valid-token")
                    .setNewPassword(TEST_NEW_PASSWORD);

            when(tokenService.validateAndConsumeToken("valid-token", "PASSWORD_RESET"))
                    .thenReturn(TEST_USER_ID);
            // 第一次selectOne查找EMAIL凭证，第二次查找密码凭证
            when(credentialMapper.selectOne(any()))
                    .thenReturn(emailCredential)
                    .thenReturn(passwordCredential);
            when(passwordHistoryMapper.selectList(any())).thenReturn(Collections.emptyList());

            // when
            bizService.resetPasswordByEmail(req);

            // then
            verify(tokenService).validateAndConsumeToken("valid-token", "PASSWORD_RESET");
            verify(credentialMapper, times(2)).selectOne(any());
            verify(credentialMapper).updateById(any(ContentUserCredential.class));
            verify(passwordHistoryMapper, times(2)).insert(any(ContentUserPasswordHistory.class));
        }

        @Test
        @DisplayName("token无效或已过期 - 抛出异常")
        void resetPasswordByEmail_expiredToken_throwsError() {
            // given
            ContentAuthResetPasswordByEmailReq req = new ContentAuthResetPasswordByEmailReq()
                    .setToken("expired-token")
                    .setNewPassword(TEST_NEW_PASSWORD);

            when(tokenService.validateAndConsumeToken("expired-token", "PASSWORD_RESET"))
                    .thenReturn(null);

            // when / then
            assertThatThrownBy(() -> bizService.resetPasswordByEmail(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("重置链接无效或已过期");
        }

        @Test
        @DisplayName("token已被使用 - 抛出异常")
        void resetPasswordByEmail_usedToken_throwsError() {
            // given
            ContentAuthResetPasswordByEmailReq req = new ContentAuthResetPasswordByEmailReq()
                    .setToken("used-token")
                    .setNewPassword(TEST_NEW_PASSWORD);

            when(tokenService.validateAndConsumeToken("used-token", "PASSWORD_RESET"))
                    .thenReturn(null);

            // when / then
            assertThatThrownBy(() -> bizService.resetPasswordByEmail(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("重置链接无效或已过期");
        }

        @Test
        @DisplayName("复用历史密码 - 抛出不能使用历史密码异常")
        void resetPasswordByEmail_reusePassword_throwsError() {
            // given
            ContentAuthResetPasswordByEmailReq req = new ContentAuthResetPasswordByEmailReq()
                    .setToken("valid-token")
                    .setNewPassword(TEST_NEW_PASSWORD);

            when(tokenService.validateAndConsumeToken("valid-token", "PASSWORD_RESET"))
                    .thenReturn(TEST_USER_ID);
            // 第一次selectOne查找EMAIL凭证，第二次查找密码凭证
            when(credentialMapper.selectOne(any()))
                    .thenReturn(emailCredential)
                    .thenReturn(passwordCredential);

            // 邮箱重置使用邮箱作为加密标识符
            // 盐值必须为8字节（PBEWithMD5AndDES要求）
            String reusedSalt = "emhist01";
            String reusedHash = PasswordUtil.encrypt(TEST_NEW_PASSWORD, TEST_EMAIL, reusedSalt);
            ContentUserPasswordHistory history = new ContentUserPasswordHistory()
                    .setUserId(TEST_USER_ID)
                    .setPasswordHash(reusedHash)
                    .setSalt(reusedSalt);

            when(passwordHistoryMapper.selectList(any()))
                    .thenReturn(Collections.singletonList(history));

            // when / then
            assertThatThrownBy(() -> bizService.resetPasswordByEmail(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("不能使用最近3次使用过的密码");
        }
    }
}
