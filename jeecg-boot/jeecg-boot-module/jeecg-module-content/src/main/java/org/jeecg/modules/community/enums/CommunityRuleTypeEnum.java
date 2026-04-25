package org.jeecg.modules.community.enums;

/**
 * 社群规则类型枚举
 * 定义社群中不同类型的规则
 * 
 * @author system
 * @since 2024-12-16
 */
public enum CommunityRuleTypeEnum {
    
    /**
     * 社群规则
     */
    COMMUNITY_RULE(1, "社群规则"),
    
    /**
     * 发帖规则
     */
    POST_RULE(2, "发帖规则"),
    
    /**
     * 评论规则
     */
    COMMENT_RULE(3, "评论规则");
    
    private final Integer value;
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param code 规则类型代码
     * @param description 规则类型描述
     */
    CommunityRuleTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
    
    /**
     * 获取规则类型代码
     * 
     * @return 规则类型代码
     */
    public Integer getValue() {
        return value;
    }
    
    /**
     * 获取规则类型描述
     * 
     * @return 规则类型描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举值
     * 
     * @param code 规则类型代码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static CommunityRuleTypeEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (CommunityRuleTypeEnum ruleType : values()) {
            if (ruleType.getValue().equals(value)) {
                return ruleType;
            }
        }
        return null;
    }
}