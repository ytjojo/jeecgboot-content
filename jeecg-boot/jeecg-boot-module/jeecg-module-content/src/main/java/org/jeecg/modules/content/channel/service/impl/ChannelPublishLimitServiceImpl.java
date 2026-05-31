package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.modules.content.channel.service.ChannelPublishLimitService;
import org.springframework.stereotype.Service;

@Service
public class ChannelPublishLimitServiceImpl implements ChannelPublishLimitService {

    @Override
    public String checkLimit(int dailyLimit, int hourlyLimit, int todayCount, int hourlyCount, int wordCount, int minWordCount) {
        if (minWordCount > 0 && wordCount < minWordCount) {
            return "WORD_COUNT_LOW";
        }
        if (dailyLimit > 0 && todayCount >= dailyLimit) {
            return "DAILY_EXCEEDED";
        }
        if (hourlyLimit > 0 && hourlyCount >= hourlyLimit) {
            return "HOURLY_EXCEEDED";
        }
        return "PASS";
    }
}
