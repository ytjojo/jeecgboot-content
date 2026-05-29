package org.jeecg.modules.content.auth.constant;

/**
 * 认证模块错误码常量。
 */
public final class AuthErrorCodeConstant {

    public static final String VERIFICATION_CODE_EXPIRED = "AUTH_001";
    public static final String VERIFICATION_CODE_INVALID = "AUTH_002";
    public static final String VERIFICATION_CODE_COOLDOWN = "AUTH_003";
    public static final String ACCOUNT_LOCKED = "AUTH_004";
    public static final String PASSWORD_INCORRECT = "AUTH_005";
    public static final String ACCOUNT_NOT_FOUND = "AUTH_006";
    public static final String ACCOUNT_ALREADY_EXISTS = "AUTH_007";
    public static final String BINDING_ALREADY_EXISTS = "AUTH_008";
    public static final String LAST_LOGIN_METHOD = "AUTH_009";
    public static final String DEVICE_LIMIT_EXCEEDED = "AUTH_010";
    public static final String CANNOT_REVOKE_CURRENT = "AUTH_011";
    public static final String PASSWORD_REUSE = "AUTH_012";
    public static final String RISK_BLOCKED = "AUTH_013";
    public static final String CANCELLATION_BLOCKED = "AUTH_014";
    public static final String CANCEL_COOLDOWN_ACTIVE = "AUTH_015";

    private AuthErrorCodeConstant() {
    }
}
