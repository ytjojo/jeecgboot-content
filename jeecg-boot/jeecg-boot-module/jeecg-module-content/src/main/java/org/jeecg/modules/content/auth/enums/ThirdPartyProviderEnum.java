package org.jeecg.modules.content.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 第三方登录提供方枚举。
 */
@Getter
@RequiredArgsConstructor
public enum ThirdPartyProviderEnum {
    WECHAT("WECHAT", "微信"),
    APPLE("APPLE", "苹果"),
    GOOGLE("GOOGLE", "谷歌");

    private final String code;
    private final String description;

    /**
     * Returns all supported third party provider codes.
     */
    public static List<String> codes() {
        return Arrays.stream(values()).map(ThirdPartyProviderEnum::getCode).collect(Collectors.toList());
    }
}
