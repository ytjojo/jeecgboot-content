package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserProfileReview;
import org.jeecg.modules.content.user.req.profile.ContentUserReviewHandleReq;

/**
 * 内容社区资料审核服务契约。
 */
public interface IContentUserProfileReviewService {

    ContentUserProfileReview getPendingReview(String userId);

    void handleReview(ContentUserReviewHandleReq req);
}
