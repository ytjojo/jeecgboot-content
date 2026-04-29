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
}
