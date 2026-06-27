package org.jeecg.modules.content.circle.growth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 圈子内成员成长等级枚举。
 * <p>
 * 与 {@link CircleLevelEnum}（圈子等级）完全独立，两套体系不可混用：
 * <ul>
 *   <li>CircleLevelEnum — 圈子等级，基于成长分(0-850)，无 userId</li>
 *   <li>MemberLevelEnum — 成员等级，基于经验值(0-1000)，有 userId</li>
 * </ul>
 * </p>
 */
@Getter
@RequiredArgsConstructor
public enum MemberLevelEnum {
    L1(1, "初来乍到", 0),
    L2(2, "小有所成", 100),
    L3(3, "圈内达人", 300),
    L4(4, "资深成员", 600),
    L5(5, "圈中领袖", 1000);

    private final int level;
    private final String name;
    private final int threshold;

    /**
     * 根据经验值计算当前等级。
     *
     * @param expPoints 累计经验值
     * @return 对应等级枚举，经验值不足最低门槛时返回 L1
     */
    public static MemberLevelEnum ofExp(int expPoints) {
        MemberLevelEnum result = L1;
        for (MemberLevelEnum e : values()) {
            if (expPoints >= e.getThreshold()) {
                result = e;
            }
        }
        return result;
    }

    /**
     * 根据等级数值查找对应枚举，用于 DB 存储的 level 字段。
     *
     * @param level 等级数值 1-5
     * @return 对应枚举，非法值返回 L1
     */
    public static MemberLevelEnum ofLevel(int level) {
        for (MemberLevelEnum e : values()) {
            if (e.getLevel() == level) {
                return e;
            }
        }
        return L1;
    }
}
