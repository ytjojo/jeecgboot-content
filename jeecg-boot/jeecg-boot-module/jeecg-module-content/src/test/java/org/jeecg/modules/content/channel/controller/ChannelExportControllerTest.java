package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelExportBiz;
import org.jeecg.modules.content.channel.entity.ChannelExportTask;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.req.ChannelExportReq;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.jeecg.modules.content.channel.service.IChannelExportTaskService;
import org.jeecg.modules.content.channel.vo.ChannelExportTaskVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelExportControllerTest {

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_USERNAME = "testuser";

    @Mock
    private ChannelExportBiz exportBiz;
    @Mock
    private IChannelExportTaskService exportTaskService;
    @Mock
    private ChannelMemberService memberService;
    @Mock
    private LambdaQueryChainWrapper<ChannelExportTask> queryWrapper;

    @InjectMocks
    private ChannelExportController controller;

    @BeforeEach
    void setUp() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(TEST_USER_ID);
        loginUser.setUsername(TEST_USERNAME);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                JSON.toJSONString(loginUser), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ChannelMember adminMember = new ChannelMember();
        adminMember.setRole(MemberRole.ADMIN.getCode());
        lenient().when(memberService.getByChannelAndUser(any(), eq(TEST_USER_ID))).thenReturn(adminMember);

        lenient().when(exportTaskService.lambdaQuery()).thenReturn(queryWrapper);
        lenient().when(queryWrapper.eq(any(), any())).thenReturn(queryWrapper);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_create_export_task() {
        ChannelExportReq req = new ChannelExportReq();
        req.setChannelId("ch1");
        ChannelExportTaskVO vo = ChannelExportTaskVO.builder().taskId("t1").status("pending").build();
        when(exportBiz.createExport(req, TEST_USER_ID)).thenReturn(vo);

        Result<ChannelExportTaskVO> result = controller.createExport(req);

        assertThat(result.isSuccess()).isTrue();
        verify(exportBiz).createExport(req, TEST_USER_ID);
    }

    @Test
    void should_return_export_status() {
        ChannelExportTask task = new ChannelExportTask();
        task.setTaskId("task1");
        task.setChannelId("ch1");
        when(queryWrapper.one()).thenReturn(task);
        ChannelExportTaskVO vo = ChannelExportTaskVO.builder().taskId("t1").status("completed").build();
        when(exportBiz.getExportStatus("task1")).thenReturn(vo);

        Result<ChannelExportTaskVO> result = controller.getExportStatus("task1");

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void should_error_when_export_task_not_found() {
        when(queryWrapper.one()).thenReturn(null);

        Result<ChannelExportTaskVO> result = controller.getExportStatus("nope");

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("导出任务不存在");
    }
}
