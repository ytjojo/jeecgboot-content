package org.jeecg.modules.content.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录身份类型枚举。
 */
@Getter
@RequiredArgsConstructor
public enum AuthIdentityTypeEnum {
    MOBILE("MOBILE", "手机号"),
    EMAIL("EMAIL", "邮箱"),
    THIRD_PARTY("THIRD_PARTY", "第三方账号");

    private final String code;
    private final String description;

    /**
     * Returns all supported identity type codes.
     */
    public static List<String> codes() {
        return Arrays.stream(values()).map(AuthIdentityTypeEnum::getCode).collect(Collectors.toList());
    }
}
