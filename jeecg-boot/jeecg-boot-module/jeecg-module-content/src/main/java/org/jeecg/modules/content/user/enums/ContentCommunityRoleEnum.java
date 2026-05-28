package org.jeecg.modules.content.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum for content community role.
 */
@Getter
@RequiredArgsConstructor
public enum ContentCommunityRoleEnum {
    NORMAL("NORMAL", "普通用户", "普通社区用户"),
    CREATOR("CREATOR", "创作者", "认证内容创作者"),
    MODERATOR("MODERATOR", "版主", "社区版主，具有内容管理权限"),
    ADMIN("ADMIN", "管理员", "社区管理员，具有全部管理权限");

    private final String code;
    private final String name;
    private final String description;

    /**
     * Returns all supported community role codes.
     */
    public static List<String> codes() {
        return Arrays.stream(values()).map(ContentCommunityRoleEnum::getCode).collect(Collectors.toList());
    }

    /**
     * Finds enum by code value, returns NORMAL if not found.
     */
    public static ContentCommunityRoleEnum fromValue(String code) {
        if (code == null || code.isBlank()) {
            return NORMAL;
        }
        return Arrays.stream(values())
            .filter(e -> e.getCode().equals(code))
            .findFirst()
            .orElse(NORMAL);
    }
}
