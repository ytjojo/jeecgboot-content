package org.jeecg.modules.content.channel.task;

import org.jeecg.modules.content.channel.service.IContentChannelRecommendationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class ChannelRecommendationRefreshTaskTest {

    @Mock
    private IContentChannelRecommendationService recommendationService;

    @InjectMocks
    private ChannelRecommendationRefreshTask refreshTask;

    @Test
    void execute_shouldRunWithoutException() {
        assertDoesNotThrow(() -> refreshTask.execute());
    }
}
