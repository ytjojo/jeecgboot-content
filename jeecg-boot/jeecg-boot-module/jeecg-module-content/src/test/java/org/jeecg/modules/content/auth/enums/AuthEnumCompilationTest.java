package org.jeecg.modules.content.auth.enums;

import org.jeecg.modules.content.auth.constant.AuthErrorCodeConstant;
import org.jeecg.modules.content.auth.constant.AuthRedisKeyConstant;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 认证模块枚举和常量编译覆盖测试。
 * 确保所有枚举值、codes() 方法和常量均可正常访问。
 */
class AuthEnumCompilationTest {

    // ---- AuthIdentityTypeEnum ----
    @Test
    void authIdentityTypeEnum_hasExpectedValues() {
        assertThat(AuthIdentityTypeEnum.values()).hasSize(3);
        assertThat(AuthIdentityTypeEnum.MOBILE.getCode()).isEqualTo("MOBILE");
        assertThat(AuthIdentityTypeEnum.MOBILE.getDescription()).isEqualTo("手机号");
        assertThat(AuthIdentityTypeEnum.EMAIL.getCode()).isEqualTo("EMAIL");
        assertThat(AuthIdentityTypeEnum.EMAIL.getDescription()).isEqualTo("邮箱");
        assertThat(AuthIdentityTypeEnum.THIRD_PARTY.getCode()).isEqualTo("THIRD_PARTY");
        assertThat(AuthIdentityTypeEnum.THIRD_PARTY.getDescription()).isEqualTo("第三方账号");
    }

    @Test
    void authIdentityTypeEnum_codes() {
        List<String> codes = AuthIdentityTypeEnum.codes();
        assertThat(codes).containsExactlyInAnyOrder("MOBILE", "EMAIL", "THIRD_PARTY");
    }

    // ---- CredentialTypeEnum ----
    @Test
    void credentialTypeEnum_hasExpectedValues() {
        assertThat(CredentialTypeEnum.values()).hasSize(4);
        assertThat(CredentialTypeEnum.PASSWORD.getCode()).isEqualTo("PASSWORD");
        assertThat(CredentialTypeEnum.PASSWORD.getDescription()).isEqualTo("密码");
        assertThat(CredentialTypeEnum.SMS_CODE.getCode()).isEqualTo("SMS_CODE");
        assertThat(CredentialTypeEnum.SMS_CODE.getDescription()).isEqualTo("短信验证码");
        assertThat(CredentialTypeEnum.EMAIL_CODE.getCode()).isEqualTo("EMAIL_CODE");
        assertThat(CredentialTypeEnum.EMAIL_CODE.getDescription()).isEqualTo("邮箱验证码");
        assertThat(CredentialTypeEnum.THIRD_PARTY.getCode()).isEqualTo("THIRD_PARTY");
        assertThat(CredentialTypeEnum.THIRD_PARTY.getDescription()).isEqualTo("第三方登录");
    }

    @Test
    void credentialTypeEnum_codes() {
        List<String> codes = CredentialTypeEnum.codes();
        assertThat(codes).containsExactlyInAnyOrder("PASSWORD", "SMS_CODE", "EMAIL_CODE", "THIRD_PARTY");
    }

    // ---- ThirdPartyProviderEnum ----
    @Test
    void thirdPartyProviderEnum_hasExpectedValues() {
        assertThat(ThirdPartyProviderEnum.values()).hasSize(3);
        assertThat(ThirdPartyProviderEnum.WECHAT.getCode()).isEqualTo("WECHAT");
        assertThat(ThirdPartyProviderEnum.WECHAT.getDescription()).isEqualTo("微信");
        assertThat(ThirdPartyProviderEnum.APPLE.getCode()).isEqualTo("APPLE");
        assertThat(ThirdPartyProviderEnum.APPLE.getDescription()).isEqualTo("苹果");
        assertThat(ThirdPartyProviderEnum.GOOGLE.getCode()).isEqualTo("GOOGLE");
        assertThat(ThirdPartyProviderEnum.GOOGLE.getDescription()).isEqualTo("谷歌");
    }

    @Test
    void thirdPartyProviderEnum_codes() {
        List<String> codes = ThirdPartyProviderEnum.codes();
        assertThat(codes).containsExactlyInAnyOrder("WECHAT", "APPLE", "GOOGLE");
    }

    // ---- DeviceSessionStatusEnum ----
    @Test
    void deviceSessionStatusEnum_hasExpectedValues() {
        assertThat(DeviceSessionStatusEnum.values()).hasSize(3);
        assertThat(DeviceSessionStatusEnum.ACTIVE.getCode()).isEqualTo("ACTIVE");
        assertThat(DeviceSessionStatusEnum.ACTIVE.getDescription()).isEqualTo("活跃");
        assertThat(DeviceSessionStatusEnum.OFFLINE.getCode()).isEqualTo("OFFLINE");
        assertThat(DeviceSessionStatusEnum.OFFLINE.getDescription()).isEqualTo("已下线");
        assertThat(DeviceSessionStatusEnum.EXPIRED.getCode()).isEqualTo("EXPIRED");
        assertThat(DeviceSessionStatusEnum.EXPIRED.getDescription()).isEqualTo("已过期");
    }

    @Test
    void deviceSessionStatusEnum_codes() {
        List<String> codes = DeviceSessionStatusEnum.codes();
        assertThat(codes).containsExactlyInAnyOrder("ACTIVE", "OFFLINE", "EXPIRED");
    }

    // ---- RiskEventTypeEnum ----
    @Test
    void riskEventTypeEnum_hasExpectedValues() {
        assertThat(RiskEventTypeEnum.values()).hasSize(4);
        assertThat(RiskEventTypeEnum.LOGIN_FAIL.getCode()).isEqualTo("LOGIN_FAIL");
        assertThat(RiskEventTypeEnum.LOGIN_FAIL.getDescription()).isEqualTo("登录失败");
        assertThat(RiskEventTypeEnum.BATCH_REGISTER.getCode()).isEqualTo("BATCH_REGISTER");
        assertThat(RiskEventTypeEnum.BATCH_REGISTER.getDescription()).isEqualTo("批量注册");
        assertThat(RiskEventTypeEnum.ABNORMAL_LOGIN.getCode()).isEqualTo("ABNORMAL_LOGIN");
        assertThat(RiskEventTypeEnum.ABNORMAL_LOGIN.getDescription()).isEqualTo("异常登录");
        assertThat(RiskEventTypeEnum.BRUTE_FORCE.getCode()).isEqualTo("BRUTE_FORCE");
        assertThat(RiskEventTypeEnum.BRUTE_FORCE.getDescription()).isEqualTo("暴力破解");
    }

    @Test
    void riskEventTypeEnum_codes() {
        List<String> codes = RiskEventTypeEnum.codes();
        assertThat(codes).containsExactlyInAnyOrder("LOGIN_FAIL", "BATCH_REGISTER", "ABNORMAL_LOGIN", "BRUTE_FORCE");
    }

    // ---- RiskDecisionEnum ----
    @Test
    void riskDecisionEnum_hasExpectedValues() {
        assertThat(RiskDecisionEnum.values()).hasSize(3);
        assertThat(RiskDecisionEnum.ALLOW.getCode()).isEqualTo("ALLOW");
        assertThat(RiskDecisionEnum.ALLOW.getDescription()).isEqualTo("放行");
        assertThat(RiskDecisionEnum.CHALLENGE.getCode()).isEqualTo("CHALLENGE");
        assertThat(RiskDecisionEnum.CHALLENGE.getDescription()).isEqualTo("挑战验证");
        assertThat(RiskDecisionEnum.BLOCK.getCode()).isEqualTo("BLOCK");
        assertThat(RiskDecisionEnum.BLOCK.getDescription()).isEqualTo("拦截");
    }

    @Test
    void riskDecisionEnum_codes() {
        List<String> codes = RiskDecisionEnum.codes();
        assertThat(codes).containsExactlyInAnyOrder("ALLOW", "CHALLENGE", "BLOCK");
    }

    // ---- CancellationStatusEnum ----
    @Test
    void cancellationStatusEnum_hasExpectedValues() {
        assertThat(CancellationStatusEnum.values()).hasSize(3);
        assertThat(CancellationStatusEnum.ACTIVE.getCode()).isEqualTo("ACTIVE");
        assertThat(CancellationStatusEnum.ACTIVE.getDescription()).isEqualTo("正常");
        assertThat(CancellationStatusEnum.CANCELLING.getCode()).isEqualTo("CANCELLING");
        assertThat(CancellationStatusEnum.CANCELLING.getDescription()).isEqualTo("注销中");
        assertThat(CancellationStatusEnum.CANCELLED.getCode()).isEqualTo("CANCELLED");
        assertThat(CancellationStatusEnum.CANCELLED.getDescription()).isEqualTo("已注销");
    }

    @Test
    void cancellationStatusEnum_codes() {
        List<String> codes = CancellationStatusEnum.codes();
        assertThat(codes).containsExactlyInAnyOrder("ACTIVE", "CANCELLING", "CANCELLED");
    }

    // ---- VerificationCodeSceneEnum ----
    @Test
    void verificationCodeSceneEnum_hasExpectedValues() {
        assertThat(VerificationCodeSceneEnum.values()).hasSize(7);
        assertThat(VerificationCodeSceneEnum.REGISTER.getCode()).isEqualTo("REGISTER");
        assertThat(VerificationCodeSceneEnum.REGISTER.getDescription()).isEqualTo("注册");
        assertThat(VerificationCodeSceneEnum.LOGIN.getCode()).isEqualTo("LOGIN");
        assertThat(VerificationCodeSceneEnum.LOGIN.getDescription()).isEqualTo("登录");
        assertThat(VerificationCodeSceneEnum.BIND_MOBILE.getCode()).isEqualTo("BIND_MOBILE");
        assertThat(VerificationCodeSceneEnum.BIND_MOBILE.getDescription()).isEqualTo("绑定手机号");
        assertThat(VerificationCodeSceneEnum.BIND_EMAIL.getCode()).isEqualTo("BIND_EMAIL");
        assertThat(VerificationCodeSceneEnum.BIND_EMAIL.getDescription()).isEqualTo("绑定邮箱");
        assertThat(VerificationCodeSceneEnum.RESET_PASSWORD.getCode()).isEqualTo("RESET_PASSWORD");
        assertThat(VerificationCodeSceneEnum.RESET_PASSWORD.getDescription()).isEqualTo("重置密码");
        assertThat(VerificationCodeSceneEnum.UNBIND_MOBILE.getCode()).isEqualTo("UNBIND_MOBILE");
        assertThat(VerificationCodeSceneEnum.UNBIND_MOBILE.getDescription()).isEqualTo("解绑手机号");
        assertThat(VerificationCodeSceneEnum.UNBIND_EMAIL.getCode()).isEqualTo("UNBIND_EMAIL");
        assertThat(VerificationCodeSceneEnum.UNBIND_EMAIL.getDescription()).isEqualTo("解绑邮箱");
    }

    @Test
    void verificationCodeSceneEnum_codes() {
        List<String> codes = VerificationCodeSceneEnum.codes();
        assertThat(codes).containsExactlyInAnyOrder(
                "REGISTER", "LOGIN", "BIND_MOBILE", "BIND_EMAIL",
                "RESET_PASSWORD", "UNBIND_MOBILE", "UNBIND_EMAIL"
        );
    }

    // ---- AuthRedisKeyConstant ----
    @Test
    void authRedisKeyConstant_keysNotNull() {
        assertThat(AuthRedisKeyConstant.CODE_PREFIX).isEqualTo("content:auth:code:");
        assertThat(AuthRedisKeyConstant.CODE_FAIL_PREFIX).isEqualTo("content:auth:code:fail:");
        assertThat(AuthRedisKeyConstant.PWD_FAIL_PREFIX).isEqualTo("content:auth:pwd_fail:");
        assertThat(AuthRedisKeyConstant.LOCK_PREFIX).isEqualTo("content:auth:lock:");
        assertThat(AuthRedisKeyConstant.TOKEN_BLACKLIST_PREFIX).isEqualTo("content:auth:token_blacklist:");
        assertThat(AuthRedisKeyConstant.RISK_COUNTER_PREFIX).isEqualTo("content:risk:counter:");
    }

    @Test
    void authRedisKeyConstant_ttlValues() {
        assertThat(AuthRedisKeyConstant.SMS_CODE_TTL).isEqualTo(300L);
        assertThat(AuthRedisKeyConstant.EMAIL_CODE_TTL).isEqualTo(86400L);
        assertThat(AuthRedisKeyConstant.PWD_FAIL_TTL).isEqualTo(900L);
        assertThat(AuthRedisKeyConstant.LOCK_TTL).isEqualTo(1800L);
        assertThat(AuthRedisKeyConstant.TOKEN_BLACKLIST_TTL).isEqualTo(86400L);
    }

    // ---- AuthErrorCodeConstant ----
    @Test
    void authErrorCodeConstant_allCodesNotNull() {
        assertThat(AuthErrorCodeConstant.VERIFICATION_CODE_EXPIRED).isEqualTo("AUTH_001");
        assertThat(AuthErrorCodeConstant.VERIFICATION_CODE_INVALID).isEqualTo("AUTH_002");
        assertThat(AuthErrorCodeConstant.VERIFICATION_CODE_COOLDOWN).isEqualTo("AUTH_003");
        assertThat(AuthErrorCodeConstant.ACCOUNT_LOCKED).isEqualTo("AUTH_004");
        assertThat(AuthErrorCodeConstant.PASSWORD_INCORRECT).isEqualTo("AUTH_005");
        assertThat(AuthErrorCodeConstant.ACCOUNT_NOT_FOUND).isEqualTo("AUTH_006");
        assertThat(AuthErrorCodeConstant.ACCOUNT_ALREADY_EXISTS).isEqualTo("AUTH_007");
        assertThat(AuthErrorCodeConstant.BINDING_ALREADY_EXISTS).isEqualTo("AUTH_008");
        assertThat(AuthErrorCodeConstant.LAST_LOGIN_METHOD).isEqualTo("AUTH_009");
        assertThat(AuthErrorCodeConstant.DEVICE_LIMIT_EXCEEDED).isEqualTo("AUTH_010");
        assertThat(AuthErrorCodeConstant.CANNOT_REVOKE_CURRENT).isEqualTo("AUTH_011");
        assertThat(AuthErrorCodeConstant.PASSWORD_REUSE).isEqualTo("AUTH_012");
        assertThat(AuthErrorCodeConstant.RISK_BLOCKED).isEqualTo("AUTH_013");
        assertThat(AuthErrorCodeConstant.CANCELLATION_BLOCKED).isEqualTo("AUTH_014");
        assertThat(AuthErrorCodeConstant.CANCEL_COOLDOWN_ACTIVE).isEqualTo("AUTH_015");
    }
}
