package org.jeecg.modules.content.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentUserVisibilityEnum {
    PUBLIC("PUBLIC", "公开"),
    FOLLOWERS_ONLY("FOLLOWERS_ONLY", "仅关注者可见"),
    MUTUAL_ONLY("MUTUAL_ONLY", "仅互关可见"),
    PRIVATE("PRIVATE", "仅自己可见");

    private final String code;
    private final String description;
}
