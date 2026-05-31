package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.service.impl.ChannelPublishLimitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChannelPublishLimitServiceTest {

    private ChannelPublishLimitServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ChannelPublishLimitServiceImpl();
    }

    @Test
    void shouldRejectWhenDailyLimitExceeded() {
        String result = service.checkLimit(5, 0, 5, 0, 200, 100);
        assertEquals("DAILY_EXCEEDED", result);
    }

    @Test
    void shouldRejectWhenHourlyLimitExceeded() {
        String result = service.checkLimit(0, 3, 0, 3, 200, 100);
        assertEquals("HOURLY_EXCEEDED", result);
    }

    @Test
    void shouldRejectWhenWordCountLow() {
        String result = service.checkLimit(5, 3, 0, 0, 50, 100);
        assertEquals("WORD_COUNT_LOW", result);
    }

    @Test
    void shouldPassWhenAllLimitsOk() {
        String result = service.checkLimit(5, 3, 2, 1, 200, 100);
        assertEquals("PASS", result);
    }

    @Test
    void shouldPassWhenLimitsAreZero() {
        String result = service.checkLimit(0, 0, 0, 0, 50, 0);
        assertEquals("PASS", result);
    }
}
