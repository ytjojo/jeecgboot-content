package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelBlacklist;
import org.jeecg.modules.content.channel.mapper.ChannelBlacklistMapper;
import org.jeecg.modules.content.channel.service.ChannelBlacklistService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChannelBlacklistServiceImpl implements ChannelBlacklistService {

    @Resource
    private ChannelBlacklistMapper blacklistMapper;

    @Override
    public void addToBlacklist(String channelId, String userId, String operatorId, String reason) {
        Long count = blacklistMapper.selectCount(new LambdaQueryWrapper<ChannelBlacklist>()
            .eq(ChannelBlacklist::getChannelId, channelId)
            .eq(ChannelBlacklist::getUserId, userId));
        if (count > 0) {
            throw new JeecgBootException("用户已在黑名单中");
        }
        ChannelBlacklist entry = new ChannelBlacklist();
        entry.setChannelId(channelId);
        entry.setUserId(userId);
        entry.setOperatorId(operatorId);
        entry.setReason(reason);
        blacklistMapper.insert(entry);
    }

    @Override
    public void removeFromBlacklist(String channelId, String userId, String operatorId) {
        ChannelBlacklist entry = blacklistMapper.selectOne(new LambdaQueryWrapper<ChannelBlacklist>()
            .eq(ChannelBlacklist::getChannelId, channelId)
            .eq(ChannelBlacklist::getUserId, userId));
        if (entry == null) {
            throw new JeecgBootException("用户不在黑名单中");
        }
        blacklistMapper.deleteById(entry.getId());
    }

    @Override
    public boolean isBlacklisted(String channelId, String userId) {
        return blacklistMapper.selectCount(new LambdaQueryWrapper<ChannelBlacklist>()
            .eq(ChannelBlacklist::getChannelId, channelId)
            .eq(ChannelBlacklist::getUserId, userId)) > 0;
    }

    @Override
    public List<String> listBlacklistedUserIds(String channelId) {
        return blacklistMapper.selectList(new LambdaQueryWrapper<ChannelBlacklist>()
            .eq(ChannelBlacklist::getChannelId, channelId))
            .stream()
            .map(ChannelBlacklist::getUserId)
            .collect(Collectors.toList());
    }
}
