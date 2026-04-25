package org.jeecg.modules.channel.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 频道状态枚举
 * 
 * @author system
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum ChannelStatusEnum {

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
    REJECTED(3, "rejected", "审核拒绝状态");

  

    /**
     * 频道状态值
     */
    @EnumValue
    @JsonValue
    private final Integer value;

    /**
     * 频道状态名称
     */
    private final String name;

    /**
     * 频道状态描述
     */
    private final String description;

    /**
     * 根据值获取枚举
     * @param value 值
     * @return 枚举
     */
    public static ChannelStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ChannelStatusEnum statusEnum : values()) {
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
    public static ChannelStatusEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (ChannelStatusEnum statusEnum : values()) {
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
    public Boolean isNormal() {
        return this == ENABLED;
    }

}