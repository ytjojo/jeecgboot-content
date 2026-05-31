package org.jeecg.modules.content.channel.service;

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

/**
 * 加入申请审核流程测试
 * 验证申请提交、去重、审批、拒绝的完整流程
 */
@ExtendWith(MockitoExtension.class)
class ChannelJoinReviewTest {

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
        // 提交加入申请时应创建PENDING状态的记录
        when(applicationMapper.selectCount(any())).thenReturn(0L);

        applicationService.apply("ch1", "user1", "希望加入");

        verify(applicationMapper).insert(any(ChannelJoinApplication.class));
    }

    @Test
    void should_reject_duplicate_pending_application() {
        // 已有待审核申请时不允许重复提交，防止刷申请
        when(applicationMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> applicationService.apply("ch1", "user1", "希望加入"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("已有待审核的申请");
    }

    @Test
    void should_approve_application() {
        // 审批通过后状态应变为APPROVED，并记录审核人和时间
        ChannelJoinApplication app = new ChannelJoinApplication();
        app.setId("app1");
        app.setStatus(ApplicationStatus.PENDING.getCode());
        when(applicationMapper.selectById("app1")).thenReturn(app);

        applicationService.approve("app1", "admin1", "欢迎加入");

        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.APPROVED.getCode());
        assertThat(app.getReviewerId()).isEqualTo("admin1");
        assertThat(app.getReviewReason()).isEqualTo("欢迎加入");
        assertThat(app.getReviewTime()).isNotNull();
    }

    @Test
    void should_reject_application() {
        // 拒绝后状态应变为REJECTED，并记录拒绝原因
        ChannelJoinApplication app = new ChannelJoinApplication();
        app.setId("app1");
        app.setStatus(ApplicationStatus.PENDING.getCode());
        when(applicationMapper.selectById("app1")).thenReturn(app);

        applicationService.reject("app1", "admin1", "不符合条件");

        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.REJECTED.getCode());
        assertThat(app.getReviewerId()).isEqualTo("admin1");
        assertThat(app.getReviewReason()).isEqualTo("不符合条件");
        assertThat(app.getReviewTime()).isNotNull();
    }

    @Test
    void should_reject_approving_already_processed_application() {
        // 已处理的申请不能再次审批，防止重复操作
        ChannelJoinApplication app = new ChannelJoinApplication();
        app.setId("app1");
        app.setStatus(ApplicationStatus.APPROVED.getCode());
        when(applicationMapper.selectById("app1")).thenReturn(app);

        assertThatThrownBy(() -> applicationService.approve("app1", "admin1", "欢迎"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("已处理");
    }

    @Test
    void should_reject_rejecting_already_processed_application() {
        // 已处理的申请不能再次拒绝
        ChannelJoinApplication app = new ChannelJoinApplication();
        app.setId("app1");
        app.setStatus(ApplicationStatus.REJECTED.getCode());
        when(applicationMapper.selectById("app1")).thenReturn(app);

        assertThatThrownBy(() -> applicationService.reject("app1", "admin1", "理由"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("已处理");
    }

    @Test
    void should_throw_when_application_not_found() {
        // 操作不存在的申请应明确报错
        when(applicationMapper.selectById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> applicationService.approve("nonexistent", "admin1", "理由"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("申请不存在");
    }
}
