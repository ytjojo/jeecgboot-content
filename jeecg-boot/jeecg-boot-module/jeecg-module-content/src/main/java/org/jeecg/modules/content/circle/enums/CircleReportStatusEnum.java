package org.jeecg.modules.content.circle.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 圈子内容举报状态枚举。
 */
@Getter
@RequiredArgsConstructor
public enum CircleReportStatusEnum {
    PENDING("PENDING", "待处理"),
    RESOLVED("RESOLVED", "已处理"),
    IGNORED("IGNORED", "已忽略");

    private final String code;
    private final String description;

    /**
     * Returns all supported report status codes.
     */
    public static List<String> codes() {
        return Arrays.stream(values()).map(CircleReportStatusEnum::getCode).collect(Collectors.toList());
    }
}
