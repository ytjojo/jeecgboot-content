package org.jeecg.modules.content.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 验证码场景枚举。
 */
@Getter
@RequiredArgsConstructor
public enum VerificationCodeSceneEnum {
    REGISTER("REGISTER", "注册"),
    LOGIN("LOGIN", "登录"),
    BIND_MOBILE("BIND_MOBILE", "绑定手机号"),
    BIND_EMAIL("BIND_EMAIL", "绑定邮箱"),
    RESET_PASSWORD("RESET_PASSWORD", "重置密码"),
    UNBIND_MOBILE("UNBIND_MOBILE", "解绑手机号"),
    UNBIND_EMAIL("UNBIND_EMAIL", "解绑邮箱");

    private final String code;
    private final String description;

    /**
     * Returns all supported verification code scene codes.
     */
    public static List<String> codes() {
        return Arrays.stream(values()).map(VerificationCodeSceneEnum::getCode).collect(Collectors.toList());
    }
}
