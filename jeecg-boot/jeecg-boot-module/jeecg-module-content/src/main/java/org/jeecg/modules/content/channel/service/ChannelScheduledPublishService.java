package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelScheduledPublish;
import java.util.List;

public interface ChannelScheduledPublishService {
    List<ChannelScheduledPublish> findDueTasks();
    void markPublished(String taskId);
    void markFailed(String taskId, String reason);
}
