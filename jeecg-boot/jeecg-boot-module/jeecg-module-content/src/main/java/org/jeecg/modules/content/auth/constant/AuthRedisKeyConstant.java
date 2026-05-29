package org.jeecg.modules.content.auth.constant;

/**
 * 认证模块 Redis Key 常量。
 */
public final class AuthRedisKeyConstant {

    public static final String CODE_PREFIX = "content:auth:code:";
    public static final String CODE_FAIL_PREFIX = "content:auth:code:fail:";
    public static final String PWD_FAIL_PREFIX = "content:auth:pwd_fail:";
    public static final String LOCK_PREFIX = "content:auth:lock:";
    public static final String TOKEN_BLACKLIST_PREFIX = "content:auth:token_blacklist:";
    public static final String RISK_COUNTER_PREFIX = "content:risk:counter:";
    public static final String LOGIN_FAIL_PREFIX = "content:risk:login_fail:";
    public static final String REGISTER_IP_PREFIX = "content:risk:register_ip:";
    public static final String COOLDOWN_PREFIX = "content:auth:code:cooldown:";

    /** 验证码发送冷却时间（秒） */
    public static final long CODE_COOLDOWN_TTL = 60L;
    /** 验证码最大失败次数 */
    public static final int CODE_MAX_FAIL_COUNT = 3;

    /** 短信验证码 TTL（秒） */
    public static final long SMS_CODE_TTL = 300L;
    /** 邮箱验证码 TTL（秒） */
    public static final long EMAIL_CODE_TTL = 86400L;
    /** 密码错误计数 TTL（秒） */
    public static final long PWD_FAIL_TTL = 900L;
    /** 登录失败计数 TTL（秒） */
    public static final long LOGIN_FAIL_TTL = 1800L;
    /** 账号锁定 TTL（秒） */
    public static final long LOCK_TTL = 1800L;
    /** Token 黑名单 TTL（秒） */
    public static final long TOKEN_BLACKLIST_TTL = 86400L;
    /** IP注册计数 TTL（秒） */
    public static final long REGISTER_IP_TTL = 3600L;

    /** 登录失败触发验证码挑战的阈值 */
    public static final int LOGIN_FAIL_CAPTCHA_THRESHOLD = 10;
    /** 登录失败触发账号锁定的阈值 */
    public static final int LOGIN_FAIL_LOCK_THRESHOLD = 20;
    /** 账号锁定时长（毫秒） */
    public static final long LOGIN_FAIL_LOCK_DURATION_MS = 30 * 60 * 1000L;
    /** IP注册限流阈值 */
    public static final int IP_REGISTER_LIMIT = 10;

    private AuthRedisKeyConstant() {
    }
}
