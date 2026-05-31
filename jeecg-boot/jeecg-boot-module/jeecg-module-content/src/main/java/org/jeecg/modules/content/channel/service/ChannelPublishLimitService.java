package org.jeecg.modules.content.channel.service;

public interface ChannelPublishLimitService {
    /**
     * 校验发布限额
     * @return PASS/DAILY_EXCEEDED/HOURLY_EXCEEDED/WORD_COUNT_LOW
     */
    String checkLimit(int dailyLimit, int hourlyLimit, int todayCount, int hourlyCount, int wordCount, int minWordCount);
}
