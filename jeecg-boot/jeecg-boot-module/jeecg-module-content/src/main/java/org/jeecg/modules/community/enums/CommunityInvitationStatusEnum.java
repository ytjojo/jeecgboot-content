package org.jeecg.modules.community.enums;

import org.jeecg.modules.common.enums.BaseEnum;

/**
 * 社群邀请状态枚举
 * 定义社群邀请的不同状态
 * 
 * @author system
 * @since 2024-12-16
 */
public enum CommunityInvitationStatusEnum implements BaseEnum<CommunityInvitationStatusEnum, Integer> {
    
    /**
     * 待接受
     */
    PENDING(0, "待接受"),
    
    /**
     * 已接受
     */
    ACCEPTED(1, "已接受"),
    
    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝"),
    
    /**
     * 已过期
     */
    EXPIRED(3, "已过期");
    
    private final Integer value;
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param code 状态代码
     * @param description 状态描述
     */
    CommunityInvitationStatusEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
    
   
    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 实现BaseEnum接口 - 获取枚举值
     * 
     * @return 枚举值（状态代码）
     */
    @Override
    public Integer getValue() {
        return value;
    }
    
    /**
     * 实现BaseEnum接口 - 获取枚举值名称
     * 
     * @return 枚举值的名称
     */
    @Override
    public String getName() {
        return name();
    }
    
    /**
     * 根据代码获取枚举值
     * 
     * @param code 状态代码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static CommunityInvitationStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CommunityInvitationStatusEnum status : values()) {
            if (status.getValue().equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为待处理状态
     * 
     * @return 如果是待处理状态返回true，否则返回false
     */
    public boolean isPending() {
        return this == PENDING;
    }
    
    /**
     * 判断是否为已处理状态（接受、拒绝、过期）
     * 
     * @return 如果是已处理状态返回true，否则返回false
     */
    public boolean isProcessed() {
        return this == ACCEPTED || this == REJECTED || this == EXPIRED;
    }
    
    /**
     * 判断是否为成功状态（已接受）
     * 
     * @return 如果是成功状态返回true，否则返回false
     */
    public boolean isSuccess() {
        return this == ACCEPTED;
    }
    
    /**
     * 判断是否为有效状态（待接受）
     * 
     * @return 如果是有效状态返回true，否则返回false
     */
    public boolean isValid() {
        return this == PENDING;
    }
}