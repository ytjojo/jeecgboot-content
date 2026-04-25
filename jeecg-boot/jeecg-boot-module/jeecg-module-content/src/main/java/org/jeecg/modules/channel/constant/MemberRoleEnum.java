package org.jeecg.modules.channel.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 频道成员角色枚举
 * 使用位运算支持多角色叠加
 * 
 * @author jeecg-boot
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum MemberRoleEnum {

    /**
     * 访客：只读权限
     */
    GUEST(0, "guest", "访客，只有查看权限"),

    /**
     * 普通成员：基础权限
     */
    MEMBER(1, "member", "普通成员，拥有基础权限"),
    /**
     * 版主：拥有内容管理权限
     */
    MODERATOR(2, "moderator", "频道版主，拥有内容管理权限"),

    /**
     * 管理员：拥有管理权限
     */
    ADMIN(3, "admin", "频道管理员，拥有管理权限"),

    /**
     * 拥有者：拥有所有权限
     */
    OWNER(4, "owner", "频道创建者，拥有所有权限");

    /**
     * 角色值（位掩码）
     */
    @EnumValue
    @JsonValue
    private final Integer value;

    /**
     * 角色名称
     */
    private final String name;

    /**
     * 角色描述
     */
    private final String description;

    /**
     * 根据值获取枚举
     * 
     * @param value 角色值
     * @return 角色枚举
     */
    public static MemberRoleEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (MemberRoleEnum role : values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        return null;
    }

    /**
     * 检查是否包含指定角色
     * 
     * @param roleMask 角色掩码
     * @param role     要检查的角色
     * @return 是否包含
     */
    public boolean hasRole(MemberRoleEnum role) {
        if (role == null) {
            return false;
        }
        return this.value >= role.getValue();
    }

   

    public Integer getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}