package org.jeecg.modules.content.user.enums;

import lombok.Getter;
import org.jeecg.common.exception.JeecgBootException;

import java.util.Arrays;
import java.util.List;

/**
 * 拉黑、屏蔽和过滤规则状态枚举。
 */
@Getter
public enum ContentUserProtectionStatusEnum {
    ACTIVE("ACTIVE"),
    CANCELLED("CANCELLED"),
    EXPIRED("EXPIRED");

    private static final int CODE_MAX_LENGTH = 32;

    private final String code;

    ContentUserProtectionStatusEnum(String code) {
        this.code = code;
    }

    public static ContentUserProtectionStatusEnum ofCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new JeecgBootException("保护状态不能为空");
        }
        String normalizedCode = code.trim().toUpperCase();
        if (normalizedCode.length() > CODE_MAX_LENGTH) {
            throw new JeecgBootException("保护状态长度不能超过32位");
        }
        return Arrays.stream(values())
            .filter(item -> item.code.equals(normalizedCode))
            .findFirst()
            .orElseThrow(() -> new JeecgBootException("保护状态不支持"));
    }

    public static List<String> codes() {
        return Arrays.stream(values()).map(ContentUserProtectionStatusEnum::getCode).toList();
    }
}
