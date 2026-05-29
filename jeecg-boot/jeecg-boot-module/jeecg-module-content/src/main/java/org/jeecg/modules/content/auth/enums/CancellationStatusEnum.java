package org.jeecg.modules.content.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账号注销状态枚举。
 */
@Getter
@RequiredArgsConstructor
public enum CancellationStatusEnum {
    ACTIVE("ACTIVE", "正常"),
    CANCELLING("CANCELLING", "注销中"),
    CANCELLED("CANCELLED", "已注销");

    private final String code;
    private final String description;

    /**
     * Returns all supported cancellation status codes.
     */
    public static List<String> codes() {
        return Arrays.stream(values()).map(CancellationStatusEnum::getCode).collect(Collectors.toList());
    }
}
