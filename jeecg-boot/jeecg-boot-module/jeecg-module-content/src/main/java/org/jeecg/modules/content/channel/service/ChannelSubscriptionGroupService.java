package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelSubscriptionGroup;

import java.util.List;

public interface ChannelSubscriptionGroupService extends IService<ChannelSubscriptionGroup> {

    ChannelSubscriptionGroup createGroup(String userId, String groupName);

    void renameGroup(String groupId, String newName, String userId);

    void deleteGroup(String groupId, String userId);

    void moveToGroup(String subscriptionId, String groupId);

    List<ChannelSubscriptionGroup> listByUser(String userId);
}
