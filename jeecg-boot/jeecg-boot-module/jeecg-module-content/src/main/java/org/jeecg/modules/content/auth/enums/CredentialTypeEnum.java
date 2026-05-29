package org.jeecg.modules.content.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 凭证类型枚举。
 */
@Getter
@RequiredArgsConstructor
public enum CredentialTypeEnum {
    PASSWORD("PASSWORD", "密码"),
    SMS_CODE("SMS_CODE", "短信验证码"),
    EMAIL_CODE("EMAIL_CODE", "邮箱验证码"),
    THIRD_PARTY("THIRD_PARTY", "第三方登录");

    private final String code;
    private final String description;

    /**
     * Returns all supported credential type codes.
     */
    public static List<String> codes() {
        return Arrays.stream(values()).map(CredentialTypeEnum::getCode).collect(Collectors.toList());
    }
}
