package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelContentGovernanceLog;
import org.jeecg.modules.content.channel.mapper.ChannelContentGovernanceLogMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelContentGovernanceLogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * 频道内容治理日志服务测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelContentGovernanceLogServiceTest {

    @Mock
    private ChannelContentGovernanceLogMapper logMapper;

    @InjectMocks
    private ChannelContentGovernanceLogServiceImpl logService;

    @Test
    void should_persist_full_audit_record() {
        logService.log("ch1", "c1", "admin", "block", "blocked by policy", "spam", "success");

        ArgumentCaptor<ChannelContentGovernanceLog> captor = ArgumentCaptor.forClass(ChannelContentGovernanceLog.class);
        verify(logMapper).insert(captor.capture());
        ChannelContentGovernanceLog log = captor.getValue();
        assertThat(log.getChannelId()).isEqualTo("ch1");
        assertThat(log.getContentId()).isEqualTo("c1");
        assertThat(log.getOperatorId()).isEqualTo("admin");
        assertThat(log.getAction()).isEqualTo("block");
        assertThat(log.getActionDetail()).isEqualTo("blocked by policy");
        assertThat(log.getReason()).isEqualTo("spam");
        assertThat(log.getResult()).isEqualTo("success");
    }

    @Test
    void should_allow_null_reason_and_result() {
        logService.log("ch1", "c1", "admin", "view", "viewed detail", null, null);

        ArgumentCaptor<ChannelContentGovernanceLog> captor = ArgumentCaptor.forClass(ChannelContentGovernanceLog.class);
        verify(logMapper).insert(captor.capture());
        assertThat(captor.getValue().getReason()).isNull();
        assertThat(captor.getValue().getResult()).isNull();
    }
}
