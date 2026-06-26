package org.jeecg.modules.content.circle.growth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 成长行为类型枚举。
 */
@Getter
@RequiredArgsConstructor
public enum GrowthActionEnum {
    POST("POST", "发帖", 10, 10),
    COMMENT("COMMENT", "评论", 3, 3),
    LIKE("LIKE", "点赞", 0, 0),
    FEATURED("FEATURED", "加精", 30, 50);

    private final String code;
    private final String description;
    private final int expPoints;
    private final int contributionPoints;
}
