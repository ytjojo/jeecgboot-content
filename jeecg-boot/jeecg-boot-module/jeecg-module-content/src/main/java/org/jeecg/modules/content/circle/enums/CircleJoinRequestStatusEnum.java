package org.jeecg.modules.content.circle.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 圈子加入申请状态枚举。
 */
@Getter
@RequiredArgsConstructor
public enum CircleJoinRequestStatusEnum {
    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已批准"),
    REJECTED("REJECTED", "已拒绝"),
    EXPIRED("EXPIRED", "已过期");

    private final String code;
    private final String description;

    /**
     * Returns all supported join request status codes.
     */
    public static List<String> codes() {
        return Arrays.stream(values()).map(CircleJoinRequestStatusEnum::getCode).collect(Collectors.toList());
    }
}
