package org.jeecg.modules.content.userstatus.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举。
 * 定义 9 种用户状态及其元数据。
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    GUEST(1, "GUEST", "游客", "未注册或匿名访问的用户"),
    REGISTERED_INCOMPLETE(2, "REGISTERED_INCOMPLETE", "已注册未完善资料", "已注册但未完成资料完善的用户"),
    NORMAL(3, "NORMAL", "正常", "正常状态用户，可正常使用所有功能"),
    MUTED(4, "MUTED", "禁言", "被禁言用户，禁止主动互动功能"),
    RESTRICTED_RECOMMEND(5, "RESTRICTED_RECOMMEND", "限制推荐", "内容在推荐流和搜索中降权或屏蔽"),
    FROZEN(6, "FROZEN", "冻结", "账号被冻结，禁止登录，需安全核验"),
    BANNED(7, "BANNED", "封禁", "账号被封禁，禁止登录和所有API访问"),
    DEACTIVATING(8, "DEACTIVATING", "注销中", "用户申请注销，等待处理"),
    DEACTIVATED(9, "DEACTIVATED", "已注销", "账号已注销完成");

    /**
     * 状态码（整数）
     */
    private final int code;

    /**
     * 状态名称（英文）
     */
    private final String name;

    /**
     * 显示名称（中文）
     */
    private final String displayName;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据状态码查找枚举值
     *
     * @param code 状态码
     * @return 对应的枚举值，如果不存在返回 null
     */
    public static UserStatusEnum fromCode(int code) {
        for (UserStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
