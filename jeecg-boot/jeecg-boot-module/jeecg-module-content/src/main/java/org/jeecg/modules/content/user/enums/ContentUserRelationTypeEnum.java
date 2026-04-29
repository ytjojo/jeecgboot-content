package org.jeecg.modules.content.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum for content user relation type.
 */
@Getter
@RequiredArgsConstructor
public enum ContentUserRelationTypeEnum {
    FOLLOW("FOLLOW", "关注"),
    SPECIAL_FOLLOW("SPECIAL_FOLLOW", "特别关注"),
    MUTE("MUTE", "屏蔽"),
    BLACKLIST("BLACKLIST", "拉黑"),
    SUBSCRIPTION("SUBSCRIPTION", "订阅");

    private final String code;
    private final String description;
}
