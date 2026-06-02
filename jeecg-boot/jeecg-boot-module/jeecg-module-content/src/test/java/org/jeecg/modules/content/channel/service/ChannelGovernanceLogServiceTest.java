package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelGovernanceLog;
import org.jeecg.modules.content.channel.enums.GovernanceAction;
import org.jeecg.modules.content.channel.mapper.ChannelGovernanceLogMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelGovernanceLogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * 频道治理日志服务测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelGovernanceLogServiceTest {

    @Mock
    private ChannelGovernanceLogMapper logMapper;

    @InjectMocks
    private ChannelGovernanceLogServiceImpl logService;

    @Test
    void should_persist_action_code_and_metadata() {
        logService.log(GovernanceAction.MUTE, "ch1", "admin1", "u2", "spam", "{\"hours\":24}");

        ArgumentCaptor<ChannelGovernanceLog> captor = ArgumentCaptor.forClass(ChannelGovernanceLog.class);
        verify(logMapper).insert(captor.capture());
        ChannelGovernanceLog log = captor.getValue();
        assertThat(log.getAction()).isEqualTo(GovernanceAction.MUTE.getCode());
        assertThat(log.getChannelId()).isEqualTo("ch1");
        assertThat(log.getOperatorId()).isEqualTo("admin1");
        assertThat(log.getTargetUserId()).isEqualTo("u2");
        assertThat(log.getReason()).isEqualTo("spam");
        assertThat(log.getExtraData()).isEqualTo("{\"hours\":24}");
    }

    @Test
    void should_record_blacklist_add_event() {
        logService.log(GovernanceAction.BLACKLIST_ADD, "ch1", "admin1", "u3", "abuse", null);

        ArgumentCaptor<ChannelGovernanceLog> captor = ArgumentCaptor.forClass(ChannelGovernanceLog.class);
        verify(logMapper).insert(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo(GovernanceAction.BLACKLIST_ADD.getCode());
        assertThat(captor.getValue().getExtraData()).isNull();
    }

    @Test
    void should_record_remove_event() {
        logService.log(GovernanceAction.REMOVE, "ch1", "admin1", "u4", "rule violation", null);

        ArgumentCaptor<ChannelGovernanceLog> captor = ArgumentCaptor.forClass(ChannelGovernanceLog.class);
        verify(logMapper).insert(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo(GovernanceAction.REMOVE.getCode());
    }
}
