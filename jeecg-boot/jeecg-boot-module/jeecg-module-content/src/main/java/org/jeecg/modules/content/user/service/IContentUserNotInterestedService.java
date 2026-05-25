package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.user.entity.ContentUserNotInterested;

/**
 * 内容社区用户不感兴趣反馈服务契约。
 */
public interface IContentUserNotInterestedService extends IService<ContentUserNotInterested> {

    /**
     * 记录不感兴趣反馈。
     */
    void recordFeedback(String userId, String contentId, String contentType);
}
