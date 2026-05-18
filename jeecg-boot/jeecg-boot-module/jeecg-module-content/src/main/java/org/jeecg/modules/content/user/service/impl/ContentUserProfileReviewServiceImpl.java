package org.jeecg.modules.content.user.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserProfileReview;
import org.jeecg.modules.content.user.mapper.ContentUserProfileReviewMapper;
import org.jeecg.modules.content.user.req.profile.ContentUserReviewHandleReq;
import org.jeecg.modules.content.user.service.IContentUserProfileReviewService;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

/**
 * 内容社区资料审核服务实现。
 */
@Service
public class ContentUserProfileReviewServiceImpl implements IContentUserProfileReviewService {

    @Resource
    private ContentUserProfileReviewMapper reviewMapper;

    @Lazy
    @Resource
    private IContentUserProfileService profileService;

    @Override
    public ContentUserProfileReview getPendingReview(String userId) {
        return reviewMapper.selectPendingByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReview(ContentUserReviewHandleReq req) {
        ContentUserProfileReview review = reviewMapper.selectById(req.getReviewId());
        if (review == null || !"PENDING".equals(review.getReviewStatus())) {
            throw new JeecgBootException("待审核资料不存在");
        }
        profileService.handleProfileReview(req);
    }
}
