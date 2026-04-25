package org.jeecg.modules.community.enums;

/**
 * 社群申请状态枚举
 * 定义社群加入申请的不同状态
 * 
 * @author system
 * @since 2024-12-16
 */
public enum CommunityJoinRequestStatusEnum {
    
    /**
     * 待审核
     */
    PENDING(0, "待审核"),
    
    /**
     * 已通过
     */
    APPROVED(1, "已通过"),
    
    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝"),
    
    /**
     * 已取消
     */
    CANCELLED(3, "已取消");
    
    private final Integer code;
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param code 状态代码
     * @param description 状态描述
     */
    CommunityJoinRequestStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取状态代码
     * 
     * @return 状态代码
     */
    public Integer getCode() {
        return code;
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
     * 根据代码获取枚举值
     * 
     * @param code 状态代码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static CommunityJoinRequestStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CommunityJoinRequestStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
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
     * 判断是否为已处理状态（通过、拒绝、取消）
     * 
     * @return 如果是已处理状态返回true，否则返回false
     */
    public boolean isProcessed() {
        return this == APPROVED || this == REJECTED || this == CANCELLED;
    }
    
    /**
     * 判断是否为成功状态（已通过）
     * 
     * @return 如果是成功状态返回true，否则返回false
     */
    public boolean isSuccess() {
        return this == APPROVED;
    }
}