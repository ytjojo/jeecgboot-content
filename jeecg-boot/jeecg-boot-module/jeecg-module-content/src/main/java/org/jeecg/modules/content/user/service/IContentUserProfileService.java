package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq;
import org.jeecg.modules.content.user.req.profile.ContentUserReviewHandleReq;
import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;
import org.jeecg.modules.content.user.vo.ContentUserProfileVO;

/**
 * Service contract for content user profile.
 */
public interface IContentUserProfileService {

    ContentUserProfileVO getProfile(String ownerUserId, String viewerUserId);

    void updateProfile(String userId, ContentUserProfileUpdateReq req);

    void updatePrivacy(String userId, ContentUserPrivacyUpdateReq req);

    void handleProfileReview(ContentUserReviewHandleReq req);
}
