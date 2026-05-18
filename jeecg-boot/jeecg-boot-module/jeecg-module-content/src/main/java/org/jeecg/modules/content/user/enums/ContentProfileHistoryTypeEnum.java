package org.jeecg.modules.content.user.enums;

import lombok.Getter;

/**
 * 资料历史类型枚举。
 */
@Getter
public enum ContentProfileHistoryTypeEnum {
    NICKNAME("NICKNAME"),
    AVATAR("AVATAR");

    private final String code;

    ContentProfileHistoryTypeEnum(String code) {
        this.code = code;
    }
}
