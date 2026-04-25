package org.jeecg.modules.community.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 社区状态枚举
 * @author system
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum CommunityStatusEnum {
    
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
     * 社区状态值
     */
    private final Integer value;
    
    /**
     * 社区状态名称
     */
    private final String name;
    
    /**
     * 社区状态描述
     */
    private final String description;
    
    /**
     * 根据值获取枚举
     * @param value 值
     * @return 枚举
     */
    public static CommunityStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (CommunityStatusEnum statusEnum : values()) {
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
    public static CommunityStatusEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (CommunityStatusEnum statusEnum : values()) {
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