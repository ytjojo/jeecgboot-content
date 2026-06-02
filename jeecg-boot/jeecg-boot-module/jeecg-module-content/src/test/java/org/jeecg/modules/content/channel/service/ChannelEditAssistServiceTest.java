package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelContentEditHistory;
import org.jeecg.modules.content.channel.mapper.ChannelContentEditHistoryMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelEditAssistServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * 频道编辑辅助服务测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelEditAssistServiceTest {

    @Mock
    private ChannelContentEditHistoryMapper editHistoryMapper;

    @InjectMocks
    private ChannelEditAssistServiceImpl editAssistService;

    @Test
    void should_record_edit_with_full_audit_fields() {
        editAssistService.recordEdit("ch1", "c1", "u1", "title", "oldT", "newT");

        ArgumentCaptor<ChannelContentEditHistory> captor = ArgumentCaptor.forClass(ChannelContentEditHistory.class);
        verify(editHistoryMapper).insert(captor.capture());
        ChannelContentEditHistory history = captor.getValue();
        assertThat(history.getChannelId()).isEqualTo("ch1");
        assertThat(history.getContentId()).isEqualTo("c1");
        assertThat(history.getEditorId()).isEqualTo("u1");
        assertThat(history.getFieldName()).isEqualTo("title");
        assertThat(history.getOldValue()).isEqualTo("oldT");
        assertThat(history.getNewValue()).isEqualTo("newT");
    }

    @Test
    void should_record_edit_with_null_values_allowed() {
        editAssistService.recordEdit("ch1", "c1", "u1", null, null, null);

        ArgumentCaptor<ChannelContentEditHistory> captor = ArgumentCaptor.forClass(ChannelContentEditHistory.class);
        verify(editHistoryMapper).insert(captor.capture());
        assertThat(captor.getValue().getFieldName()).isNull();
    }
}
