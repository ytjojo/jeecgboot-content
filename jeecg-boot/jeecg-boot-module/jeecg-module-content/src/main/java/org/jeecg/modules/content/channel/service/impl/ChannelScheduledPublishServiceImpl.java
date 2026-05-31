package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelScheduledPublish;
import org.jeecg.modules.content.channel.enums.PublishStatusEnum;
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
               .eq(ChannelScheduledPublish::getPublishStatus, PublishStatusEnum.SCHEDULED.getCode());
        return scheduledPublishMapper.selectList(wrapper);
    }

    @Override
    public void markPublished(String taskId) {
        ChannelScheduledPublish task = scheduledPublishMapper.selectById(taskId);
        if (task == null) {
            throw new JeecgBootException("定时发布任务不存在: " + taskId);
        }
        task.setPublishStatus(PublishStatusEnum.PUBLISHED.getCode());
        scheduledPublishMapper.updateById(task);
    }

    @Override
    public void markFailed(String taskId, String reason) {
        ChannelScheduledPublish task = scheduledPublishMapper.selectById(taskId);
        if (task == null) {
            throw new JeecgBootException("定时发布任务不存在: " + taskId);
        }
        task.setPublishStatus(PublishStatusEnum.FAILED.getCode());
        task.setFailReason(reason);
        scheduledPublishMapper.updateById(task);
    }
}
