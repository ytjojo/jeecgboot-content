package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ContentChannelSearchFeedback;
import org.jeecg.modules.content.channel.mapper.ContentChannelSearchFeedbackMapper;
import org.jeecg.modules.content.channel.service.IContentChannelSearchFeedbackService;
import org.springframework.stereotype.Service;

@Service
public class ContentChannelSearchFeedbackServiceImpl
        extends ServiceImpl<ContentChannelSearchFeedbackMapper, ContentChannelSearchFeedback>
        implements IContentChannelSearchFeedbackService {

    @Override
    public void recordFeedback(String userId, String keyword, String channelId, String action) {
        ContentChannelSearchFeedback feedback = new ContentChannelSearchFeedback();
        feedback.setUserId(userId);
        feedback.setKeyword(keyword);
        feedback.setChannelId(channelId);
        feedback.setAction(action);
        save(feedback);
    }
}
