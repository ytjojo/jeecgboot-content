package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.modules.content.channel.entity.ChannelRecycleBin;
import org.jeecg.modules.content.channel.mapper.ChannelRecycleBinMapper;
import org.jeecg.modules.content.channel.service.ChannelRecycleBinService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class ChannelRecycleBinServiceImpl implements ChannelRecycleBinService {

    @Resource
    private ChannelRecycleBinMapper recycleBinMapper;

    @Override
    public ChannelRecycleBin addToRecycleBin(String channelId, String contentId, String contentType, String authorId, String deletedBy, String reason) {
        ChannelRecycleBin bin = new ChannelRecycleBin();
        bin.setChannelId(channelId);
        bin.setContentId(contentId);
        bin.setContentType(contentType);
        bin.setOriginalAuthorId(authorId);
        bin.setDeletedBy(deletedBy);
        bin.setDeleteTime(new Date());
        bin.setDeleteReason(reason);
        bin.setExpireTime(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
        bin.setIsRestored(false);
        recycleBinMapper.insert(bin);
        return bin;
    }

    @Override
    public boolean restore(String recycleBinId, String restoredBy) {
        ChannelRecycleBin bin = recycleBinMapper.selectById(recycleBinId);
        if (bin == null) {
            throw new IllegalArgumentException("回收站记录不存在: " + recycleBinId);
        }
        if (bin.getIsRestored()) {
            return false;
        }
        if (bin.getExpireTime().before(new Date())) {
            return false;
        }
        bin.setIsRestored(true);
        bin.setRestoredBy(restoredBy);
        bin.setRestoreTime(new Date());
        recycleBinMapper.updateById(bin);
        return true;
    }
}
