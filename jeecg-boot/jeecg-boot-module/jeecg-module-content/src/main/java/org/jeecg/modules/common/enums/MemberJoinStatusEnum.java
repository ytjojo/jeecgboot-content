package org.jeecg.modules.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 成员状态枚举
 * 定义成员的各种状态
 * 
 * @author jeecg-boot
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum MemberJoinStatusEnum implements BaseEnum< MemberJoinStatusEnum,Integer> {          

    /**
     * 已退出：成员已主动退出
     */
    EXITED(0, "exited", "已退出"),

    /**
     * 正常：成员状态正常，可以正常参与活动
     */
    NORMAL(1, "normal", "正常"),


    /**
     * 待审核：成员申请加入，等待管理员审核
     */
    PENDING(2, "pending", "待审核"),

    /**
     * 被踢出：成员被管理员踢出
     */
    KICKED(3, "kicked", "被踢出"),

    /**
     * 申请被拒绝：成员申请加入被管理员拒绝
     */
    REJECTED(4, "rejected", "申请被拒绝"),

    /**
     * 邀请中：成员被邀请加入，等待用户确认
     */
    INVITING(5, "inviting", "邀请中");

    /**
     * 状态值
     */
    @EnumValue
    @JsonValue
    private final Integer value;

    /**
     * 状态代码
     */
    private final String name;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据值获取枚举
     * 
     * @param value 状态值
     * @return 状态枚举
     */
    public static MemberJoinStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (MemberJoinStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据名称获取枚举
     * 
     * @param name 状态名称
     * @return 状态枚举
     */
    public static MemberJoinStatusEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (MemberJoinStatusEnum status : values()) {
            if (status.getName().equals(name)) {    
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为活跃状态（正常状态）
     * 
     * @return 是否为活跃状态
     */
    public boolean isActive() {
        return this == NORMAL;
    }

    /**
     * 判断是否为非活跃状态（已退出、被踢出、申请被拒绝）
     * 
     * @return 是否为非活跃状态
     */
    public boolean isInactive() {
        return this == EXITED || this == KICKED || this == REJECTED;
    }

    /**
     * 判断是否为待处理状态（待审核、邀请中）
     * 
     * @return 是否为待处理状态
     */
    public boolean isPending() {
        return this == PENDING || this == INVITING;
    }

   

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }
}