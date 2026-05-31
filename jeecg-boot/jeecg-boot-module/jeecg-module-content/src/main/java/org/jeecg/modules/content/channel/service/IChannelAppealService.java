package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelAppeal;

public interface IChannelAppealService extends IService<ChannelAppeal> {

    ChannelAppeal submitAppeal(String channelId, String lifecycleLogId, String applicantId, String appealReason, String attachmentUrls);

    ChannelAppeal handleAppeal(String appealId, String handlerId, String action, String handleResult);
}
