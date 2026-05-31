package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelScheduledPublish;
import org.jeecg.modules.content.channel.mapper.ChannelScheduledPublishMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelScheduledPublishServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelScheduledPublishServiceTest {

    @InjectMocks
    private ChannelScheduledPublishServiceImpl service;

    @Mock
    private ChannelScheduledPublishMapper scheduledPublishMapper;

    @Test
    void findDueTasks_shouldReturnTasksBeforeNow() {
        ChannelScheduledPublish task = new ChannelScheduledPublish();
        task.setId("task-1");
        task.setPublishStatus("SCHEDULED");
        when(scheduledPublishMapper.selectList(any())).thenReturn(Arrays.asList(task));

        List<ChannelScheduledPublish> tasks = service.findDueTasks();
        assertEquals(1, tasks.size());
    }

    @Test
    void markFailed_shouldUpdateStatusAndReason() {
        ChannelScheduledPublish task = new ChannelScheduledPublish();
        task.setId("task-1");
        when(scheduledPublishMapper.selectById("task-1")).thenReturn(task);
        when(scheduledPublishMapper.updateById(any(ChannelScheduledPublish.class))).thenReturn(1);

        service.markFailed("task-1", "用户已被禁言");
        assertEquals("FAILED", task.getPublishStatus());
        assertEquals("用户已被禁言", task.getFailReason());
    }
}
