package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelSubscription;
import org.jeecg.modules.content.channel.enums.PrivacyType;
import org.jeecg.modules.content.channel.mapper.ChannelSubscriptionMapper;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.service.ChannelSubscriptionService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ChannelSubscriptionServiceImpl extends ServiceImpl<ChannelSubscriptionMapper, ChannelSubscription>
    implements ChannelSubscriptionService {

    @Resource
    private ChannelService channelService;

    @Override
    public void subscribe(String channelId, String userId) {
        Channel channel = channelService.getById(channelId);
        if (channel == null) {
            throw new JeecgBootException("频道不存在");
        }
        if (channel.getPrivacy() != null && channel.getPrivacy() == PrivacyType.PRIVATE.getCode()) {
            throw new JeecgBootException("私有频道需先加入才能订阅");
        }
        Long count = count(new LambdaQueryWrapper<ChannelSubscription>()
            .eq(ChannelSubscription::getChannelId, channelId)
            .eq(ChannelSubscription::getUserId, userId));
        if (count > 0) {
            throw new JeecgBootException("已订阅该频道");
        }
        ChannelSubscription sub = new ChannelSubscription();
        sub.setChannelId(channelId);
        sub.setUserId(userId);
        sub.setSource(1);
        sub.setRemindEnabled(1);
        baseMapper.insert(sub);
    }

    @Override
    public void unsubscribe(String channelId, String userId) {
        ChannelSubscription sub = getOne(new LambdaQueryWrapper<ChannelSubscription>()
            .eq(ChannelSubscription::getChannelId, channelId)
            .eq(ChannelSubscription::getUserId, userId));
        if (sub == null) {
            throw new JeecgBootException("未订阅该频道");
        }
        removeById(sub.getId());
    }

    @Override
    public boolean isSubscribed(String channelId, String userId) {
        return count(new LambdaQueryWrapper<ChannelSubscription>()
            .eq(ChannelSubscription::getChannelId, channelId)
            .eq(ChannelSubscription::getUserId, userId)) > 0;
    }

    @Override
    public List<ChannelSubscription> listByUser(String userId) {
        return list(new LambdaQueryWrapper<ChannelSubscription>()
            .eq(ChannelSubscription::getUserId, userId)
            .orderByDesc(ChannelSubscription::getCreateTime));
    }
}
