package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelAppeal;
import org.jeecg.modules.content.channel.mapper.ChannelAppealMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelAppealServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道申诉服务测试
 *
 * 注意：handleAppeal 内部使用 lambdaQuery().one()，在纯 Mockito 单元测试中
 * 会触发 MyBatis-Plus "Unable to retrieve the mapperInterface" 异常
 * （lambda cache 未初始化）。此处只覆盖 submitAppeal 公开契约。
 */
@ExtendWith(MockitoExtension.class)
class ChannelAppealServiceTest {

    @Mock
    private ChannelAppealMapper appealMapper;

    @InjectMocks
    private ChannelAppealServiceImpl appealService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(appealService, "baseMapper", appealMapper);
    }

    @Test
    void should_submit_pending_appeal() {
        ChannelAppeal appeal = appealService.submitAppeal("ch1", "log1", "user1", "reason", "url1");

        assertThat(appeal.getStatus()).isEqualTo("pending");
        assertThat(appeal.getChannelId()).isEqualTo("ch1");
        assertThat(appeal.getLifecycleLogId()).isEqualTo("log1");
        assertThat(appeal.getAppealId()).isNotBlank();
        assertThat(appeal.getCreatedTime()).isNotNull();
        assertThat(appeal.getUpdatedTime()).isNotNull();
        assertThat(appeal.getAttachmentUrls()).isEqualTo("url1");
        verify(appealMapper).insert(appeal);
    }

    @Test
    void should_submit_with_null_attachment() {
        ChannelAppeal appeal = appealService.submitAppeal("ch1", "log1", "user1", "reason", null);

        assertThat(appeal.getStatus()).isEqualTo("pending");
        assertThat(appeal.getAttachmentUrls()).isNull();
    }

    @Test
    void should_initialize_audit_timestamps_on_submit() {
        ChannelAppeal appeal = appealService.submitAppeal("ch1", "log1", "user1", "reason", null);

        assertThat(appeal.getCreatedTime()).isNotNull();
        assertThat(appeal.getUpdatedTime()).isNotNull();
    }
}
