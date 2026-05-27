package org.jeecg.modules.content.user.service;

/**
 * Service contract for content user visibility policy.
 */
public interface IContentUserVisibilityPolicyService {

    boolean canViewField(String ownerUserId, String viewerUserId, String visibility);

    boolean canViewContent(String ownerUserId, String viewerUserId);

    boolean canSearchUser(String ownerUserId, String viewerUserId);

    boolean canSendPrivateMessage(String ownerUserId, String viewerUserId);

    boolean canMention(String ownerUserId, String viewerUserId);

    /**
     * 判断查看者是否可以看到目标用户的在线状态。
     * 支持三级可见性：PUBLIC（公开）、HIDDEN（隐藏）、MUTUAL_ONLY（仅互关可见）。
     */
    boolean canViewOnlineStatus(String ownerUserId, String viewerUserId);

    /**
     * 判断查看者是否可以看到目标用户的活动状态。
     * 支持四级可见性：PUBLIC（公开）、PRIVATE（仅自己）、FOLLOWERS_ONLY（仅粉丝）、MUTUAL_ONLY（仅互关）。
     */
    boolean canViewActivity(String ownerUserId, String viewerUserId, String visibility);

    /**
     * 判断指定用户的资料页面是否应返回 noindex 指令。
     * 当用户禁用搜索引擎索引时返回 true。
     */
    boolean shouldNoindexProfile(String profileUserId);
}
