package org.jeecg.modules.content.auth.biz;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.auth.entity.ContentUserCredential;
import org.jeecg.modules.content.auth.enums.CredentialTypeEnum;
import org.jeecg.modules.content.auth.enums.VerificationCodeSceneEnum;
import org.jeecg.modules.content.auth.mapper.ContentUserCredentialMapper;
import org.jeecg.modules.content.auth.req.ContentAuthBindEmailReq;
import org.jeecg.modules.content.auth.req.ContentAuthBindMobileReq;
import org.jeecg.modules.content.auth.service.IContentVerificationCodeService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 手机号/邮箱绑定业务编排测试。
 * 覆盖场景：绑定成功、格式错误、已占用、验证码无效。
 */
@ExtendWith(MockitoExtension.class)
class ContentAuthBizServiceBindTest {

    @Mock
    private IContentVerificationCodeService codeService;
    @Mock
    private ContentUserCredentialMapper credentialMapper;

    @InjectMocks
    private ContentAuthBizServiceImpl bizService;

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // ========== bindMobile 成功 ==========

    @Test
    void bindMobile_success_shouldCreateCredentialWithVerifiedTrue() {
        // given
        ContentAuthBindMobileReq req = buildValidMobileReq();
        when(codeService.verifyCode(VerificationCodeSceneEnum.BIND_MOBILE, "13800138000", "123456")).thenReturn(true);
        when(credentialMapper.selectOne(any())).thenReturn(null);  // 未被占用

        // when
        bizService.bindMobile(req);

        // then
        ArgumentCaptor<ContentUserCredential> captor = ArgumentCaptor.forClass(ContentUserCredential.class);
        verify(credentialMapper).insert(captor.capture());
        ContentUserCredential saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("u_test_001");
        assertThat(saved.getCredentialType()).isEqualTo(CredentialTypeEnum.SMS_CODE.getCode());
        assertThat(saved.getCredentialValue()).isEqualTo("13800138000");
        assertThat(saved.getVerified()).isTrue();
        assertThat(saved.getVerifyTime()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
    }

    // ========== bindMobile 手机号格式错误 ==========

    @Test
    void bindMobile_nullMobile_shouldFailValidation() {
        ContentAuthBindMobileReq req = new ContentAuthBindMobileReq()
                .setUserId("u_test_001")
                .setMobile(null)
                .setCode("123456");

        Set<ConstraintViolation<ContentAuthBindMobileReq>> violations = validator.validate(req);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("手机号不能为空"));
    }

    @Test
    void bindMobile_invalidMobileFormat_shouldFailValidation() {
        ContentAuthBindMobileReq req = new ContentAuthBindMobileReq()
                .setUserId("u_test_001")
                .setMobile("12345")
                .setCode("123456");

        Set<ConstraintViolation<ContentAuthBindMobileReq>> violations = validator.validate(req);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("手机号格式不正确"));
    }

    // ========== bindMobile 验证码为空 ==========

    @Test
    void bindMobile_emptyCode_shouldFailValidation() {
        ContentAuthBindMobileReq req = new ContentAuthBindMobileReq()
                .setUserId("u_test_001")
                .setMobile("13800138000")
                .setCode("");

        Set<ConstraintViolation<ContentAuthBindMobileReq>> violations = validator.validate(req);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("验证码不能为空"));
    }

    // ========== bindMobile 手机号已被占用 ==========

    @Test
    void bindMobile_alreadyOccupied_shouldThrowException() {
        // given
        ContentAuthBindMobileReq req = buildValidMobileReq();
        when(codeService.verifyCode(VerificationCodeSceneEnum.BIND_MOBILE, "13800138000", "123456")).thenReturn(true);
        ContentUserCredential existing = new ContentUserCredential().setUserId("u_other");
        when(credentialMapper.selectOne(any())).thenReturn(existing);

        // when & then
        assertThatThrownBy(() -> bizService.bindMobile(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("手机号已被其他账号绑定");
    }

    // ========== bindMobile 验证码无效/过期 ==========

    @Test
    void bindMobile_invalidCode_shouldThrowException() {
        // given
        ContentAuthBindMobileReq req = buildValidMobileReq();
        when(codeService.verifyCode(VerificationCodeSceneEnum.BIND_MOBILE, "13800138000", "123456")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> bizService.bindMobile(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("验证码无效或已过期");
    }

    // ========== bindEmail 成功 ==========

    @Test
    void bindEmail_success_shouldCreateCredentialWithVerifiedTrue() {
        // given
        ContentAuthBindEmailReq req = buildValidEmailReq();
        when(codeService.verifyCode(VerificationCodeSceneEnum.BIND_EMAIL, "test@example.com", "654321")).thenReturn(true);
        when(credentialMapper.selectOne(any())).thenReturn(null);  // 未被占用

        // when
        bizService.bindEmail(req);

        // then
        ArgumentCaptor<ContentUserCredential> captor = ArgumentCaptor.forClass(ContentUserCredential.class);
        verify(credentialMapper).insert(captor.capture());
        ContentUserCredential saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("u_test_001");
        assertThat(saved.getCredentialType()).isEqualTo(CredentialTypeEnum.EMAIL_CODE.getCode());
        assertThat(saved.getCredentialValue()).isEqualTo("test@example.com");
        assertThat(saved.getVerified()).isTrue();
        assertThat(saved.getVerifyTime()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
    }

    // ========== bindEmail 邮箱格式错误 ==========

    @Test
    void bindEmail_nullEmail_shouldFailValidation() {
        ContentAuthBindEmailReq req = new ContentAuthBindEmailReq()
                .setUserId("u_test_001")
                .setEmail(null)
                .setCode("654321");

        Set<ConstraintViolation<ContentAuthBindEmailReq>> violations = validator.validate(req);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("邮箱不能为空"));
    }

    @Test
    void bindEmail_invalidEmailFormat_shouldFailValidation() {
        ContentAuthBindEmailReq req = new ContentAuthBindEmailReq()
                .setUserId("u_test_001")
                .setEmail("not-an-email")
                .setCode("654321");

        Set<ConstraintViolation<ContentAuthBindEmailReq>> violations = validator.validate(req);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("邮箱格式不正确"));
    }

    // ========== bindEmail 邮箱已被占用 ==========

    @Test
    void bindEmail_alreadyOccupied_shouldThrowException() {
        // given
        ContentAuthBindEmailReq req = buildValidEmailReq();
        when(codeService.verifyCode(VerificationCodeSceneEnum.BIND_EMAIL, "test@example.com", "654321")).thenReturn(true);
        ContentUserCredential existing = new ContentUserCredential().setUserId("u_other");
        when(credentialMapper.selectOne(any())).thenReturn(existing);

        // when & then
        assertThatThrownBy(() -> bizService.bindEmail(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("邮箱已被其他账号绑定");
    }

    // ========== bindEmail 验证码无效/过期 ==========

    @Test
    void bindEmail_invalidCode_shouldThrowException() {
        // given
        ContentAuthBindEmailReq req = buildValidEmailReq();
        when(codeService.verifyCode(VerificationCodeSceneEnum.BIND_EMAIL, "test@example.com", "654321")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> bizService.bindEmail(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("验证码无效或已过期");
    }

    // ========== 辅助方法 ==========

    private ContentAuthBindMobileReq buildValidMobileReq() {
        return new ContentAuthBindMobileReq()
                .setUserId("u_test_001")
                .setMobile("13800138000")
                .setCode("123456");
    }

    private ContentAuthBindEmailReq buildValidEmailReq() {
        return new ContentAuthBindEmailReq()
                .setUserId("u_test_001")
                .setEmail("test@example.com")
                .setCode("654321");
    }
}
