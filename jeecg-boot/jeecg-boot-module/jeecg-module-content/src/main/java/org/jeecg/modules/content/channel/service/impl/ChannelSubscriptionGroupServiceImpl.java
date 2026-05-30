package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelSubscriptionGroup;
import org.jeecg.modules.content.channel.mapper.ChannelSubscriptionGroupMapper;
import org.jeecg.modules.content.channel.service.ChannelSubscriptionGroupService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelSubscriptionGroupServiceImpl extends ServiceImpl<ChannelSubscriptionGroupMapper, ChannelSubscriptionGroup>
    implements ChannelSubscriptionGroupService {

    @Override
    public ChannelSubscriptionGroup createGroup(String userId, String groupName) {
        ChannelSubscriptionGroup group = new ChannelSubscriptionGroup();
        group.setUserId(userId);
        group.setGroupName(groupName);
        group.setSortOrder(0);
        baseMapper.insert(group);
        return group;
    }

    @Override
    public void renameGroup(String groupId, String newName, String userId) {
        ChannelSubscriptionGroup group = getById(groupId);
        if (group == null || !group.getUserId().equals(userId)) {
            throw new JeecgBootException("分组不存在");
        }
        group.setGroupName(newName);
        updateById(group);
    }

    @Override
    public void deleteGroup(String groupId, String userId) {
        ChannelSubscriptionGroup group = getById(groupId);
        if (group == null || !group.getUserId().equals(userId)) {
            throw new JeecgBootException("分组不存在");
        }
        removeById(groupId);
    }

    @Override
    public void moveToGroup(String subscriptionId, String groupId) {
        // Implementation for moving subscription to group
        // Would need subscription_group_rel table operations
    }

    @Override
    public List<ChannelSubscriptionGroup> listByUser(String userId) {
        return list(new LambdaQueryWrapper<ChannelSubscriptionGroup>()
            .eq(ChannelSubscriptionGroup::getUserId, userId)
            .orderByAsc(ChannelSubscriptionGroup::getSortOrder));
    }
}
