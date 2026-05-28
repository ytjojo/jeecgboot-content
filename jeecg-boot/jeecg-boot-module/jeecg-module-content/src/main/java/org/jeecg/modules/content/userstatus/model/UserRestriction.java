package org.jeecg.modules.content.userstatus.model;

import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;

import java.util.*;

/**
 * 用户功能限制定义。
 * 定义每种状态对应的受限操作列表。
 */
public class UserRestriction {

    /**
     * 功能限制规则表：Map<状态, Set<受限功能>>
     */
    private static final Map<UserStatusEnum, Set<String>> RESTRICTION_RULES = new EnumMap<>(UserStatusEnum.class);

    static {
        // GUEST: 无法发布、互动、私信
        RESTRICTION_RULES.put(UserStatusEnum.GUEST, new HashSet<>(Arrays.asList(
            "publish", "comment", "like", "favorite", "message", "follow"
        )));

        // REGISTERED_INCOMPLETE: 无法发布、互动
        RESTRICTION_RULES.put(UserStatusEnum.REGISTERED_INCOMPLETE, new HashSet<>(Arrays.asList(
            "publish", "comment", "like", "favorite", "message", "follow"
        )));

        // NORMAL: 无限制
        RESTRICTION_RULES.put(UserStatusEnum.NORMAL, Collections.emptySet());

        // MUTED: 禁止发布和评论，允许浏览和私信
        RESTRICTION_RULES.put(UserStatusEnum.MUTED, new HashSet<>(Arrays.asList(
            "publish", "comment"
        )));

        // RESTRICTED_RECOMMEND: 内容不进入推荐流
        RESTRICTION_RULES.put(UserStatusEnum.RESTRICTED_RECOMMEND, new HashSet<>(Arrays.asList(
            "recommend"
        )));

        // FROZEN: 禁止登录
        RESTRICTION_RULES.put(UserStatusEnum.FROZEN, new HashSet<>(Arrays.asList(
            "login", "publish", "comment", "like", "favorite", "message", "follow"
        )));

        // BANNED: 禁止所有操作
        RESTRICTION_RULES.put(UserStatusEnum.BANNED, new HashSet<>(Arrays.asList(
            "login", "api", "publish", "comment", "like", "favorite", "message", "follow"
        )));

        // DEACTIVATING: 禁止所有操作
        RESTRICTION_RULES.put(UserStatusEnum.DEACTIVATING, new HashSet<>(Arrays.asList(
            "login", "api", "publish", "comment", "like", "favorite", "message", "follow"
        )));

        // DEACTIVATED: 禁止所有操作
        RESTRICTION_RULES.put(UserStatusEnum.DEACTIVATED, new HashSet<>(Arrays.asList(
            "login", "api", "publish", "comment", "like", "favorite", "message", "follow"
        )));
    }

    /**
     * 检查用户状态是否限制了指定功能
     *
     * @param status   用户状态
     * @param function 功能名称
     * @return 如果功能被限制返回 true，否则返回 false
     */
    public static boolean isRestricted(UserStatusEnum status, String function) {
        Set<String> restrictions = RESTRICTION_RULES.get(status);
        return restrictions != null && restrictions.contains(function);
    }

    /**
     * 获取指定状态的所有受限功能
     *
     * @param status 用户状态
     * @return 受限功能集合，如果状态不存在返回空集合
     */
    public static Set<String> getRestrictions(UserStatusEnum status) {
        return RESTRICTION_RULES.getOrDefault(status, Collections.emptySet());
    }

    /**
     * 检查用户状态是否允许登录
     *
     * @param status 用户状态
     * @return 如果允许登录返回 true，否则返回 false
     */
    public static boolean canLogin(UserStatusEnum status) {
        return !isRestricted(status, "login");
    }

    /**
     * 检查用户状态是否允许发布内容
     *
     * @param status 用户状态
     * @return 如果允许发布返回 true，否则返回 false
     */
    public static boolean canPublish(UserStatusEnum status) {
        return !isRestricted(status, "publish");
    }

    /**
     * 检查用户状态是否允许评论
     *
     * @param status 用户状态
     * @return 如果允许评论返回 true，否则返回 false
     */
    public static boolean canComment(UserStatusEnum status) {
        return !isRestricted(status, "comment");
    }

    /**
     * 检查用户状态是否允许发送私信
     *
     * @param status 用户状态
     * @return 如果允许发送私信返回 true，否则返回 false
     */
    public static boolean canSendMessage(UserStatusEnum status) {
        return !isRestricted(status, "message");
    }
}
