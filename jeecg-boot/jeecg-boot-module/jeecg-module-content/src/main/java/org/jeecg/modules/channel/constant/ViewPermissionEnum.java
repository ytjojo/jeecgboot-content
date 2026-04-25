package org.jeecg.modules.channel.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jeecg.modules.common.enums.BaseEnum;

/**
 * 频道查看权限枚举
 * 定义频道内容的查看权限级别：0-任何人 1-所有成员 2-管理员 3-指定成员
 * 
 * @author system
 * @since 2024-12-16
 */
@Getter
@AllArgsConstructor
public enum ViewPermissionEnum implements BaseEnum<ViewPermissionEnum, Integer> {

    /**
     * 任何人都可以查看
     * 包括未登录用户和非频道成员
     */
    ANYONE(0, "任何人", "任何人都可以查看频道内容"),

    /**
     * 所有成员可以查看
     * 仅限频道成员可以查看内容
     */
    ALL_MEMBERS(1, "所有成员", "所有频道成员都可以查看内容"),

    /**
     * 管理员可以查看
     * 仅限频道管理员和所有者可以查看内容
     */
    ADMIN_ONLY(2, "管理员", "仅管理员可以查看内容"),

    /**
     * 指定成员可以查看
     * 仅限特定指定的成员可以查看内容
     */
    SPECIFIED_MEMBERS(3, "指定成员", "仅指定成员可以查看内容");

    /**
     * 权限级别值，用于数据库存储
     */
    @EnumValue
    @JsonValue
    private final Integer value;

    /**
     * 权限级别名称，用于显示
     */
    private final String name;

    /**
     * 权限级别描述，用于详细说明
     */
    private final String description;

    /**
     * BaseEnum: 获取枚举值（代码）
     * @return 权限级别值
     */
    @Override
    public Integer getValue() {
        return value;
    }

    /**
     * BaseEnum: 获取枚举名称
     * @return 枚举常量名称
     */
    @Override
    public String getName() {
        return name();
    }

    /**
     * BaseEnum: 获取枚举值描述
     * @return 权限级别描述
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * 根据权限值获取对应的枚举实例
     * @param value 权限级别值
     * @return 对应的枚举实例，如果未找到则返回null
     */
    public static ViewPermissionEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ViewPermissionEnum permission : values()) {
            if (permission.getValue().equals(value)) {
                return permission;
            }
        }
        return null;
    }

    /**
     * 根据权限名称获取对应的枚举实例
     * @param name 权限级别名称
     * @return 对应的枚举实例，如果未找到则返回null
     */
    public static ViewPermissionEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (ViewPermissionEnum permission : values()) {
            if (permission.name.equals(name)) {
                return permission;
            }
        }
        return null;
    }

    /**
     * 判断是否为公开权限（任何人都可以查看）
     * @return 如果是公开权限返回true，否则返回false
     */
    public boolean isPublic() {
        return this == ANYONE;
    }

    /**
     * 判断是否需要成员身份才能查看
     * @return 如果需要成员身份返回true，否则返回false
     */
    public boolean requiresMembership() {
        return this != ANYONE;
    }

    /**
     * 判断是否需要管理员权限才能查看
     * @return 如果需要管理员权限返回true，否则返回false
     */
    public boolean requiresAdminPermission() {
        return this == ADMIN_ONLY;
    }

    /**
     * 判断是否需要特定指定才能查看
     * @return 如果需要特定指定返回true，否则返回false
     */
    public boolean requiresSpecificPermission() {
        return this == SPECIFIED_MEMBERS;
    }

    /**
     * 获取权限级别的严格程度（数值越大越严格）
     * @return 权限严格程度
     */
    public int getStrictnessLevel() {
        return value;
    }

    /**
     * 比较两个权限级别的严格程度
     * @param other 另一个权限级别
     * @return 如果当前权限更严格返回正数，相等返回0，更宽松返回负数
     */
    public int compareStrictness(ViewPermissionEnum other) {
        if (other == null) {
            return 1;
        }
        return Integer.compare(this.value, other.value);
    }
}