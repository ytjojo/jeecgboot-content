package org.jeecg.modules.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证状态枚举
 * 用于定义用户认证的状态类型
 * 
 * @author system
 * @since 2024-01-20
 */
@Getter
@AllArgsConstructor
@Schema(description = "认证状态枚举")
public enum VerifyStatus implements BaseEnum<VerifyStatus,Integer> {
    
    /**
     * 未认证
     */
    NOT_VERIFIED(0,"NOT_VERIFIED", "未认证"),
    
    /**
     * 认证中
     */
    VERIFYING(1,"VERIFYING", "认证中"),
    
    /**
     * 已认证
     */
    VERIFIED(2,"VERIFIED", "已认证"),
    
    /**
     * 认证失败
     */
    VERIFY_FAILED(3,"VERIFY_FAILED", "认证失败");
    
    /**
     * 状态码，存储到数据库的值
     */
    @EnumValue
    @JsonValue
    private final Integer value;

    private final String name;
    
    /**
     * 状态描述
     */
    private final String description;
    
    /**
     * 根据状态码获取枚举值
     * 
     * @param code 状态码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static VerifyStatus getByCode(Integer value) {
        if (value == null) {
            return null;
        }
        for (VerifyStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为已认证状态
     * 
     * @return true-已认证，false-未认证或其他状态
     */
    public boolean isVerified() {
        return this == VERIFIED;
    }
    
    /**
     * 判断是否为认证中状态
     * 
     * @return true-认证中，false-其他状态
     */
    public boolean isVerifying() {
        return this == VERIFYING;
    }
    
    /**
     * 判断是否可以进行认证操作
     * 只有未认证和认证失败状态才可以重新认证
     * 
     * @return true-可以认证，false-不可以认证
     */
    public boolean canVerify() {
        return this == NOT_VERIFIED || this == VERIFY_FAILED;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
    /**
     * 获取状态描述信息
     * 
     * @return 状态描述
     */
    @Override
    public String getDescription() {
        return description;
    }
}