package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelJoinApplication;
import org.jeecg.modules.content.channel.enums.ApplicationStatus;
import org.jeecg.modules.content.channel.mapper.ChannelJoinApplicationMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelJoinApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelJoinApplicationServiceTest {

    @Mock
    private ChannelJoinApplicationMapper applicationMapper;

    @InjectMocks
    private ChannelJoinApplicationServiceImpl applicationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(applicationService, "baseMapper", applicationMapper);
    }

    @Test
    void should_submit_application() {
        when(applicationMapper.selectCount(any())).thenReturn(0L);

        applicationService.apply("ch1", "user1", "希望加入");

        verify(applicationMapper).insert(any(ChannelJoinApplication.class));
    }

    @Test
    void should_reject_duplicate_application() {
        when(applicationMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> applicationService.apply("ch1", "user1", "希望加入"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("已有待审核的申请");
    }

    @Test
    void should_approve_application() {
        ChannelJoinApplication app = new ChannelJoinApplication();
        app.setId("app1");
        app.setStatus(ApplicationStatus.PENDING.getCode());
        when(applicationMapper.selectById("app1")).thenReturn(app);

        applicationService.approve("app1", "admin1", "欢迎加入");

        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.APPROVED.getCode());
        assertThat(app.getReviewerId()).isEqualTo("admin1");
    }

    @Test
    void should_reject_already_processed_application() {
        ChannelJoinApplication app = new ChannelJoinApplication();
        app.setId("app1");
        app.setStatus(ApplicationStatus.APPROVED.getCode());
        when(applicationMapper.selectById("app1")).thenReturn(app);

        assertThatThrownBy(() -> applicationService.approve("app1", "admin1", "欢迎"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("已处理");
    }
}
