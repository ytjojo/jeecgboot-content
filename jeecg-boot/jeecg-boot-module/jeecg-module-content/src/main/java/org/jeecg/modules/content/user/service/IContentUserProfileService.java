package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserPrivacySetting;
import org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq;
import org.jeecg.modules.content.user.req.profile.ContentUserReviewHandleReq;
import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;
import org.jeecg.modules.content.user.vo.ContentUserProfileVO;

/**
 * Service contract for content user profile.
 */
public interface IContentUserProfileService {

    ContentUserProfileVO getProfile(String ownerUserId, String viewerUserId);

    ContentUserProfileVO updateProfile(String userId, ContentUserProfileUpdateReq req);

    void updatePrivacy(String userId, ContentUserPrivacyUpdateReq req);

    /**
     * 查询用户隐私设置，null 字段填充默认值。
     */
    ContentUserPrivacySetting getPrivacySetting(String userId);

    void handleProfileReview(ContentUserReviewHandleReq req);

    int initializeCompatibilityData();
}
