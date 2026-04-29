package org.jeecg.modules.content.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum for content certification type.
 */
@Getter
@RequiredArgsConstructor
public enum ContentCertificationTypeEnum {
    PERSONAL("PERSONAL", "个人认证"),
    BIG_V("BIG_V", "大V认证"),
    OFFICIAL("OFFICIAL", "系统官方认证"),
    ENTERPRISE("ENTERPRISE", "企业认证"),
    ORGANIZATION("ORGANIZATION", "机构认证");

    private final String code;
    private final String description;
}
