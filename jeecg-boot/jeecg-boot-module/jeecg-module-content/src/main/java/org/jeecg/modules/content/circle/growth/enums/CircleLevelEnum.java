package org.jeecg.modules.content.circle.growth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 圈子等级枚举。
 */
@Getter
@RequiredArgsConstructor
public enum CircleLevelEnum {
    L1(1, "新芽圈", 0),
    L2(2, "活跃圈", 100),
    L3(3, "优质圈", 300),
    L4(4, "热门圈", 600),
    L5(5, "标杆圈", 850);

    private final int level;
    private final String name;
    private final int threshold;

    public static CircleLevelEnum ofScore(int score) {
        CircleLevelEnum result = L1;
        for (CircleLevelEnum e : values()) {
            if (score >= e.getThreshold()) {
                result = e;
            }
        }
        return result;
    }

    /**
     * 根据等级数值返回对应枚举，用于 levelName 与 DB 存储的 level 字段保持一致。
     */
    public static CircleLevelEnum ofLevel(int level) {
        for (CircleLevelEnum e : values()) {
            if (e.getLevel() == level) {
                return e;
            }
        }
        return L1;
    }
}
