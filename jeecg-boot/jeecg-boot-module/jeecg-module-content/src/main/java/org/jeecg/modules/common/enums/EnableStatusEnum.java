package org.jeecg.modules.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 启用状态枚举
 * @author system
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum EnableStatusEnum implements BaseEnum<EnableStatusEnum,Integer>  {
    
 /**
     * 禁用
     */
    DISABLED(0, "disabled", "禁用状态"),
    
    /**
     * 启用
     */
    ENABLED(1, "enabled", "启用状态"),
    
    /**
     * 审核中
     */
    REVIEWING(2, "reviewing", "审核中状态"),
    
    /**
     * 审核拒绝
     */
    REJECTED(3, "rejected", "审核拒绝状态"),

    /**
     * 删除
     */
    DELETED(-1, "deleted", "删除状态");

    /**
     * 状态值
     */
    @EnumValue
    @JsonValue
    private final Integer value;
    
    /**
     * 状态名称
     */
    private final String name;
    
    /**
     * 状态描述
     */
    private final String description;
    
    /**
     * 根据值获取枚举
     * @param value 值
     * @return 枚举
     */
    public static EnableStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (EnableStatusEnum statusEnum : values()) {
            if (statusEnum.getValue().equals(value)) {
                return statusEnum;
            }
        }
        return null;
    }
    
    /**
     * 根据名称获取枚举
     * @param name 名称
     * @return 枚举
     */
    public static EnableStatusEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (EnableStatusEnum statusEnum : values()) {
            if (statusEnum.getName().equals(name)) {
                return statusEnum;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为正常状态
     * @return 是否正常
     */
    public boolean isNormal() {
        return this == ENABLED;
    }
    
    /**
     * 判断是否为禁用状态
     * @return 是否禁用
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }
    
    /**
     * 判断是否为审核中状态
     * @return 是否审核中
     */
    public boolean isReviewing() {
        return this == REVIEWING;
    }
}