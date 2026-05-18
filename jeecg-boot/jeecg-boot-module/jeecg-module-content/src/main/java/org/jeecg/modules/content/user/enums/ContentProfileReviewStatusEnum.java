package org.jeecg.modules.content.user.enums;

import lombok.Getter;

/**
 * 资料审核状态枚举。
 */
@Getter
public enum ContentProfileReviewStatusEnum {
    NONE("NONE"),
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private final String code;

    ContentProfileReviewStatusEnum(String code) {
        this.code = code;
    }
}
