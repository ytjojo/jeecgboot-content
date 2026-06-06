package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ContentChannelSearchFeedback;

public interface IContentChannelSearchFeedbackService extends IService<ContentChannelSearchFeedback> {

    void recordFeedback(String userId, String keyword, String channelId, String action);
}
