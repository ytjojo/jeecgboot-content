package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.vo.ContentUserRelationVO;

public interface IContentUserRelationService {

    void follow(String operatorUserId, String targetUserId, String relationGroupId);

    void unfollow(String operatorUserId, String targetUserId);

    void blacklist(String operatorUserId, String targetUserId);

    void mute(String operatorUserId, String targetUserId);

    ContentUserRelationVO getRelation(String operatorUserId, String targetUserId);
}
