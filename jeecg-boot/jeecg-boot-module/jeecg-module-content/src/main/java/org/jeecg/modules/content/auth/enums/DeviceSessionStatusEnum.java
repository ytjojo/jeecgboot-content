package org.jeecg.modules.content.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备会话状态枚举。
 */
@Getter
@RequiredArgsConstructor
public enum DeviceSessionStatusEnum {
    ACTIVE("ACTIVE", "活跃"),
    OFFLINE("OFFLINE", "已下线"),
    EXPIRED("EXPIRED", "已过期");

    private final String code;
    private final String description;

    /**
     * Returns all supported device session status codes.
     */
    public static List<String> codes() {
        return Arrays.stream(values()).map(DeviceSessionStatusEnum::getCode).collect(Collectors.toList());
    }
}
