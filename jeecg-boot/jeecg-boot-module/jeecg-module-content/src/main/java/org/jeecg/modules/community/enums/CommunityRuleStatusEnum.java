package org.jeecg.modules.community.enums;

import org.jeecg.modules.common.enums.BaseEnum;

/**
 * 社群规则状态枚举
 * 定义社群规则的不同状态：0-草稿 1-启用 2-禁用
 *
 * @author system
 * @since 2024-12-16
 */
public enum CommunityRuleStatusEnum implements BaseEnum<CommunityRuleStatusEnum, Integer> {

    /** 草稿 */
    DRAFT(0, "草稿"),

    /** 启用 */
    ENABLED(1, "启用"),

    /** 禁用 */
    DISABLED(2, "禁用");

    private final Integer value;
    private final String description;

    CommunityRuleStatusEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }


    /** 获取状态描述 */
    @Override
    public String getDescription() {
        return description;
    }

    /** BaseEnum: 获取枚举值（代码） */
    @Override
    public Integer getValue() {
        return value;
    }

    /** BaseEnum: 获取枚举名称 */
    @Override
    public String getName() {
        return name();
    }

    /** 根据代码获取枚举 */
    public static CommunityRuleStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CommunityRuleStatusEnum e : values()) {
            if (e.getValue().equals(code)) {
                return e;
            }
        }
        return null;
    }

    /** 是否草稿 */
    public boolean isDraft() {
        return this == DRAFT;
    }

    /** 是否启用 */
    public boolean isEnabled() {
        return this == ENABLED;
    }

    /** 是否禁用 */
    public boolean isDisabled() {
        return this == DISABLED;
    }
}