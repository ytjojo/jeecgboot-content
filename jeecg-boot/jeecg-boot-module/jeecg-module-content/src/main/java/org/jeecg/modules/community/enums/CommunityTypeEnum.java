package org.jeecg.modules.community.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 社区类型枚举
 * @author system
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum CommunityTypeEnum {
    
    /**
     * 公开
     */
    PUBLIC(1, "公开", "公开社区"),
    
    /**
     * 私有
     */
    PRIVATE(2, "私有", "私有社区"),
    
    /**
     * 邀请制
     */
    INVITATION(3, "邀请制", "邀请制社区");
    
    /**
     * 社区类型值
     */
    private final Integer value;
    
    /**
     * 社区类型名称
     */
    private final String name;
    
    /**
     * 社区类型描述
     */
    private final String description;
    
    /**
     * 根据值获取枚举
     * @param value 值
     * @return 枚举
     */
    public static CommunityTypeEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (CommunityTypeEnum communityTypeEnum : values()) {
            if (communityTypeEnum.getValue().equals(value)) {
                return communityTypeEnum;
            }
        }
        return null;
    }
    
    /**
     * 根据名称获取枚举
     * @param name 名称
     * @return 枚举
     */
    public static CommunityTypeEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (CommunityTypeEnum communityTypeEnum : values()) {
            if (communityTypeEnum.getName().equals(name)) {
                return communityTypeEnum;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为公开社区
     * @return 是否公开
     */
    public boolean isPublic() {
        return this == PUBLIC;
    }
    
    /**
     * 判断是否为私有社区
     * @return 是否私有
     */
    public boolean isPrivate() {
        return this == PRIVATE;
    }
    
    /**
     * 判断是否为邀请制社区
     * @return 是否邀请制
     */
    public boolean isInvitation() {
        return this == INVITATION;
    }
}