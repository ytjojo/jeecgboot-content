package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserNotInterested;
import org.jeecg.modules.content.user.mapper.ContentUserNotInterestedMapper;
import org.jeecg.modules.content.user.service.IContentUserNotInterestedService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 内容社区用户不感兴趣反馈服务实现。
 */
@Service
public class ContentUserNotInterestedServiceImpl
    extends ServiceImpl<ContentUserNotInterestedMapper, ContentUserNotInterested>
    implements IContentUserNotInterestedService {

    private static final int CONTENT_ID_MAX_LENGTH = 128;
    private static final int CONTENT_TYPE_MAX_LENGTH = 64;
    private static final String ACTIVE_STATUS = "ACTIVE";

    @Resource
    private ContentUserNotInterestedMapper notInterestedMapper;

    @Override
    public void recordFeedback(String userId, String contentId, String contentType) {
        requireValidUserId(userId);
        requireValidContentId(contentId);
        requireValidContentType(contentType);

        ContentUserNotInterested feedback = new ContentUserNotInterested()
            .setUserId(userId)
            .setContentId(contentId)
            .setContentType(contentType)
            .setFeedbackTime(new Date())
            .setStatus(ACTIVE_STATUS);
        notInterestedMapper.insert(feedback);
    }

    private void requireValidUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new JeecgBootException("用户ID不能为空");
        }
    }

    private void requireValidContentId(String contentId) {
        if (contentId == null || contentId.trim().isEmpty()) {
            throw new JeecgBootException("内容ID不能为空");
        }
        if (contentId.length() > CONTENT_ID_MAX_LENGTH) {
            throw new JeecgBootException("内容ID长度不能超过128位");
        }
    }

    private void requireValidContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new JeecgBootException("内容类型不能为空");
        }
        if (contentType.length() > CONTENT_TYPE_MAX_LENGTH) {
            throw new JeecgBootException("内容类型长度不能超过64位");
        }
    }
}
