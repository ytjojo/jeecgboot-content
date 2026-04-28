package org.jeecg.modules.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证类型枚举
 * 用于定义用户认证的类型分类
 * 
 * @author system
 * @since 2024-01-20
 */
@Getter
@AllArgsConstructor
@Schema(description = "认证类型枚举")
public enum VerifyType implements BaseEnum<VerifyType,Integer>  {
    
    /**
     * 无认证类型
     */
    NONE(0,"NONE", "无"),
    
    /**
     * 个人认证
     */
    PERSONAL(1,"PERSONAL", "个人认证"),
    
    /**
     * 大V牛人认证
     */
    BIG_V(2,"BIG_V", "大v牛人认证"),
    
    /**
     * 系统官方认证
     */
    OFFICIAL(3,"OFFICIAL", "系统官方认证"),
    
    /**
     * 企业认证
     */
    ENTERPRISE(4,"ENTERPRISE", "企业认证"),
    
    /**
     * 机构认证
     */
    INSTITUTION(5,"INSTITUTION", "机构认证");
    
    /**
     * 类型码，存储到数据库的值
     */
    @EnumValue
    @JsonValue
    private final Integer value;
    
    private final String name;
    
    /**
     * 类型描述
     */
    private final String description;
    
    /**
     * 根据类型码获取枚举值
     * 
     * @param value 类型码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static VerifyType getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (VerifyType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为个人类型认证
     * 
     * @return true-个人认证，false-其他类型
     */
    public boolean isPersonal() {
        return this == PERSONAL;
    }
    
    /**
     * 判断是否为企业类型认证
     * 
     * @return true-企业认证，false-其他类型
     */
    public boolean isEnterprise() {
        return this == ENTERPRISE;
    }
    
    /**
     * 判断是否为机构类型认证
     * 
     * @return true-机构认证，false-其他类型
     */
    public boolean isInstitution() {
        return this == INSTITUTION;
    }
    
    /**
     * 判断是否为官方认证类型（包括大V、官方、企业、机构）
     * 
     * @return true-官方认证类型，false-个人或无认证
     */
    public boolean isOfficialType() {
        return this == BIG_V || this == OFFICIAL || this == ENTERPRISE || this == INSTITUTION;
    }
    
    /**
     * 判断是否需要提供认证材料
     * 除了无认证类型，其他都需要提供认证材料
     * 
     * @return true-需要认证材料，false-不需要
     */
    public boolean requiresVerificationMaterial() {
        return this != NONE;
    }
}