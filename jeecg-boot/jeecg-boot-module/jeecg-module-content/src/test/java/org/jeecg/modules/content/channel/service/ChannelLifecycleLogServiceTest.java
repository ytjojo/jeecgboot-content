package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.mapper.ChannelLifecycleLogMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelLifecycleLogServiceImpl;
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
 * 频道生命周期日志服务测试
 * 当前 Impl 为空（仅 ServiceImpl 继承），只验证注入与 BaseMapper 绑定
 */
@ExtendWith(MockitoExtension.class)
class ChannelLifecycleLogServiceTest {

    @Mock
    private ChannelLifecycleLogMapper mapper;

    @InjectMocks
    private ChannelLifecycleLogServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
    }

    @Test
    void should_use_base_mapper_for_updates() {
        ChannelLifecycleLog log = new ChannelLifecycleLog();
        log.setId("log1");

        service.updateById(log);

        verify(mapper).updateById(log);
    }

    @Test
    void should_use_base_mapper_for_remove() {
        service.removeById("log1");

        verify(mapper).deleteById("log1");
    }

    @Test
    void should_delegate_get_by_id() {
        ChannelLifecycleLog log = new ChannelLifecycleLog();
        log.setId("log1");
        when(mapper.selectById("log1")).thenReturn(log);

        ChannelLifecycleLog result = service.getById("log1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("log1");
    }

    @Test
    void should_delegate_save() {
        ChannelLifecycleLog log = new ChannelLifecycleLog();
        log.setChannelId("ch1");

        service.save(log);

        verify(mapper).insert(log);
    }
}
