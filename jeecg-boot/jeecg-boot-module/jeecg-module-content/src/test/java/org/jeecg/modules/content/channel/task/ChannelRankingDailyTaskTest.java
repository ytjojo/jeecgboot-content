package org.jeecg.modules.content.channel.task;

import org.jeecg.modules.content.channel.service.IContentChannelRankingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class ChannelRankingDailyTaskTest {

    @Mock
    private IContentChannelRankingService rankingService;

    @InjectMocks
    private ChannelRankingDailyTask rankingDailyTask;

    @Test
    void execute_shouldRunWithoutException() {
        assertDoesNotThrow(() -> rankingDailyTask.execute());
    }
}
