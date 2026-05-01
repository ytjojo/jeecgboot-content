package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.vo.ContentUserRelationVO;

/**
 * Service contract for content user relation.
 */
public interface IContentUserRelationService {

    void follow(String operatorUserId, String targetUserId, String relationGroupId);

    void specialFollow(String operatorUserId, String targetUserId, String relationGroupId);

    void cancelSpecialFollow(String operatorUserId, String targetUserId);

    void unfollow(String operatorUserId, String targetUserId);

    void blacklist(String operatorUserId, String targetUserId);

    void unblacklist(String operatorUserId, String targetUserId);

    void mute(String operatorUserId, String targetUserId);

    void unmute(String operatorUserId, String targetUserId);

    ContentUserRelationVO getRelation(String operatorUserId, String targetUserId);
}
