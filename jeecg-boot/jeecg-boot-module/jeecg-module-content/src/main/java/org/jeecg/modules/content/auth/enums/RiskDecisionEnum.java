package org.jeecg.modules.content.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 风险决策枚举。
 */
@Getter
@RequiredArgsConstructor
public enum RiskDecisionEnum {
    ALLOW("ALLOW", "放行"),
    CHALLENGE("CHALLENGE", "挑战验证"),
    BLOCK("BLOCK", "拦截");

    private final String code;
    private final String description;

    /**
     * Returns all supported risk decision codes.
     */
    public static List<String> codes() {
        return Arrays.stream(values()).map(RiskDecisionEnum::getCode).collect(Collectors.toList());
    }
}
