package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelExportTask;
import org.jeecg.modules.content.channel.mapper.ChannelExportTaskMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelExportTaskServiceImpl;
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
 * 频道导出任务服务测试
 * 当前 Impl 为空，验证 IService 契约 + baseMapper 注入
 */
@ExtendWith(MockitoExtension.class)
class ChannelExportTaskServiceTest {

    @Mock
    private ChannelExportTaskMapper mapper;

    @InjectMocks
    private ChannelExportTaskServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
    }

    @Test
    void should_inherit_save_behavior() {
        ChannelExportTask task = new ChannelExportTask();
        task.setChannelId("ch1");
        task.setUserId("u1");

        service.save(task);

        verify(mapper).insert(task);
    }

    @Test
    void should_delegate_get_by_id() {
        ChannelExportTask task = new ChannelExportTask();
        task.setId("t1");
        task.setChannelId("ch1");
        when(mapper.selectById("t1")).thenReturn(task);

        ChannelExportTask result = service.getById("t1");

        assertThat(result).isNotNull();
        assertThat(result.getChannelId()).isEqualTo("ch1");
    }

    @Test
    void should_delegate_remove() {
        service.removeById("t1");

        verify(mapper).deleteById("t1");
    }

    @Test
    void should_delegate_update() {
        ChannelExportTask task = new ChannelExportTask();
        task.setId("t1");
        task.setStatus("completed");

        service.updateById(task);

        verify(mapper).updateById(task);
    }
}
