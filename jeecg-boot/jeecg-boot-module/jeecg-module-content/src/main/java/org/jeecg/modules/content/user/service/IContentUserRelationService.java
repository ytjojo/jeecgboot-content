package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.req.relation.ContentRelationGroupReq;
import org.jeecg.modules.content.user.vo.ContentBlockMuteHelpVO;
import org.jeecg.modules.content.user.vo.ContentRelationBatchResultVO;
import org.jeecg.modules.content.user.vo.ContentRelationGroupVO;
import org.jeecg.modules.content.user.vo.ContentRelationUserPageVO;
import org.jeecg.modules.content.user.vo.ContentFollowFeedPageVO;
import org.jeecg.modules.content.user.vo.ContentUserBlacklistPageVO;
import org.jeecg.modules.content.user.vo.ContentUserRelationVO;

import java.util.List;

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

    List<ContentRelationGroupVO> listGroups(String operatorUserId);

    ContentRelationGroupVO createGroup(String operatorUserId, ContentRelationGroupReq req);

    ContentRelationGroupVO renameGroup(String operatorUserId, String groupId, ContentRelationGroupReq req);

    void deleteGroup(String operatorUserId, String groupId);

    ContentRelationBatchResultVO moveTargetsToGroup(String operatorUserId, List<String> targetUserIds, String groupId);

    ContentRelationBatchResultVO removeTargetsFromGroup(String operatorUserId, List<String> targetUserIds);

    ContentRelationBatchResultVO batchUnfollow(String operatorUserId, List<String> targetUserIds);

    ContentRelationBatchResultVO batchCancelSpecialFollow(String operatorUserId, List<String> targetUserIds);

    ContentRelationUserPageVO listFollowedUsers(String operatorUserId, String relationGroupId, String keyword, Long pageNo, Long pageSize);

    ContentRelationUserPageVO listSpecialFollowedUsers(String operatorUserId, Long pageNo, Long pageSize);

    ContentUserBlacklistPageVO listBlacklist(String operatorUserId, Long pageNo, Long pageSize);

    ContentFollowFeedPageVO listFollowFeed(String operatorUserId, Long pageNo, Long pageSize);

    /**
     * 返回拉黑、屏蔽、解除拉黑的确认文案和帮助说明。
     */
    ContentBlockMuteHelpVO getBlockMuteHelp();
}
