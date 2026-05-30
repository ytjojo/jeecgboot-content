package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelJoinApplication;

import java.util.List;

public interface ChannelJoinApplicationService extends IService<ChannelJoinApplication> {

    void apply(String channelId, String userId, String reason);

    void approve(String applicationId, String reviewerId, String reviewReason);

    void reject(String applicationId, String reviewerId, String reviewReason);

    List<ChannelJoinApplication> listPending(String channelId);
}
