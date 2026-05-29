package org.jeecg.modules.content.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 风险事件类型枚举。
 */
@Getter
@RequiredArgsConstructor
public enum RiskEventTypeEnum {
    LOGIN_FAIL("LOGIN_FAIL", "登录失败"),
    BATCH_REGISTER("BATCH_REGISTER", "批量注册"),
    ABNORMAL_LOGIN("ABNORMAL_LOGIN", "异常登录"),
    BRUTE_FORCE("BRUTE_FORCE", "暴力破解");

    private final String code;
    private final String description;

    /**
     * Returns all supported risk event type codes.
     */
    public static List<String> codes() {
        return Arrays.stream(values()).map(RiskEventTypeEnum::getCode).collect(Collectors.toList());
    }
}
