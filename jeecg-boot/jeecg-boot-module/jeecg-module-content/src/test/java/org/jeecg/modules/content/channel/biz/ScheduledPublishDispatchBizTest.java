package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.biz.impl.ScheduledPublishDispatchBizImpl;
import org.jeecg.modules.content.channel.entity.ChannelScheduledPublish;
import org.jeecg.modules.content.channel.service.ChannelContentGovernanceLogService;
import org.jeecg.modules.content.channel.service.ChannelScheduledPublishService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Collections;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledPublishDispatchBizTest {

    @InjectMocks
    private ScheduledPublishDispatchBizImpl biz;

    @Mock
    private ChannelScheduledPublishService scheduledPublishService;

    @Mock
    private ChannelPublishBiz publishBiz;

    @Mock
    private ChannelContentGovernanceLogService governanceLogService;

    @Test
    void shouldSkipWhenNoDueTasks() {
        when(scheduledPublishService.findDueTasks()).thenReturn(Collections.emptyList());

        biz.dispatch();

        verify(scheduledPublishService).findDueTasks();
        verifyNoInteractions(publishBiz);
    }

    @Test
    void shouldMarkFailedWhenTaskHasNoPublisher() {
        ChannelScheduledPublish task = new ChannelScheduledPublish();
        task.setId("task-1");
        task.setChannelId("ch-1");
        task.setContentId("content-1");
        task.setContentType("article");
        task.setPublisherId(null);
        when(scheduledPublishService.findDueTasks()).thenReturn(Arrays.asList(task));

        biz.dispatch();

        verify(scheduledPublishService).markFailed("task-1", "发布者信息缺失");
    }
}
