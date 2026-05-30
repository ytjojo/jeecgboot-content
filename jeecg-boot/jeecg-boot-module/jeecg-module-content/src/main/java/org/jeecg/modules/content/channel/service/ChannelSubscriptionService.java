package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelSubscription;

import java.util.List;

public interface ChannelSubscriptionService extends IService<ChannelSubscription> {

    void subscribe(String channelId, String userId);

    void unsubscribe(String channelId, String userId);

    boolean isSubscribed(String channelId, String userId);

    List<ChannelSubscription> listByUser(String userId);
}
