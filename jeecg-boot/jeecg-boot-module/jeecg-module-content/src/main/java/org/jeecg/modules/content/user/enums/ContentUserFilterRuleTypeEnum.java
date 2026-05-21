package org.jeecg.modules.content.user.enums;

import lombok.Getter;
import org.jeecg.common.exception.JeecgBootException;

import java.util.Arrays;
import java.util.List;

/**
 * 内容社区用户屏蔽规则类型枚举。
 */
@Getter
public enum ContentUserFilterRuleTypeEnum {
    WORD("WORD"),
    REGEX("REGEX"),
    TOPIC("TOPIC"),
    CONTENT_TYPE("CONTENT_TYPE");

    private static final int CODE_MAX_LENGTH = 32;

    private final String code;

    ContentUserFilterRuleTypeEnum(String code) {
        this.code = code;
    }

    public static ContentUserFilterRuleTypeEnum ofCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new JeecgBootException("屏蔽规则类型不能为空");
        }
        String normalizedCode = code.trim().toUpperCase();
        if (normalizedCode.length() > CODE_MAX_LENGTH) {
            throw new JeecgBootException("屏蔽规则类型长度不能超过32位");
        }
        return Arrays.stream(values())
            .filter(item -> item.code.equals(normalizedCode))
            .findFirst()
            .orElseThrow(() -> new JeecgBootException("屏蔽规则类型不支持"));
    }

    public static List<String> codes() {
        return Arrays.stream(values()).map(ContentUserFilterRuleTypeEnum::getCode).toList();
    }
}
