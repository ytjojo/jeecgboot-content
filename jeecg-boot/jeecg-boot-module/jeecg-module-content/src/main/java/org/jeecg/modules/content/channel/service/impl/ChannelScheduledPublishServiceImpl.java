package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.entity.ChannelScheduledPublish;
import org.jeecg.modules.content.channel.mapper.ChannelScheduledPublishMapper;
import org.jeecg.modules.content.channel.service.ChannelScheduledPublishService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class ChannelScheduledPublishServiceImpl implements ChannelScheduledPublishService {

    @Resource
    private ChannelScheduledPublishMapper scheduledPublishMapper;

    @Override
    public List<ChannelScheduledPublish> findDueTasks() {
        LambdaQueryWrapper<ChannelScheduledPublish> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(ChannelScheduledPublish::getScheduledTime, new Date())
               .eq(ChannelScheduledPublish::getPublishStatus, "SCHEDULED");
        return scheduledPublishMapper.selectList(wrapper);
    }

    @Override
    public void markPublished(String taskId) {
        ChannelScheduledPublish task = scheduledPublishMapper.selectById(taskId);
        task.setPublishStatus("PUBLISHED");
        scheduledPublishMapper.updateById(task);
    }

    @Override
    public void markFailed(String taskId, String reason) {
        ChannelScheduledPublish task = scheduledPublishMapper.selectById(taskId);
        task.setPublishStatus("FAILED");
        task.setFailReason(reason);
        scheduledPublishMapper.updateById(task);
    }
}
