package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.modules.content.channel.entity.ChannelContentEditHistory;
import org.jeecg.modules.content.channel.mapper.ChannelContentEditHistoryMapper;
import org.jeecg.modules.content.channel.service.ChannelEditAssistService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChannelEditAssistServiceImpl implements ChannelEditAssistService {

    @Resource
    private ChannelContentEditHistoryMapper editHistoryMapper;

    @Override
    public void recordEdit(String channelId, String contentId, String editorId, String fieldName, String oldValue, String newValue) {
        ChannelContentEditHistory history = new ChannelContentEditHistory();
        history.setChannelId(channelId);
        history.setContentId(contentId);
        history.setEditorId(editorId);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        editHistoryMapper.insert(history);
    }
}
