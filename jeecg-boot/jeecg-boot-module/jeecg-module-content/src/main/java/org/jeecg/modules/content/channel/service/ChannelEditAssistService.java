package org.jeecg.modules.content.channel.service;

public interface ChannelEditAssistService {
    void recordEdit(String channelId, String contentId, String editorId, String fieldName, String oldValue, String newValue);
}
