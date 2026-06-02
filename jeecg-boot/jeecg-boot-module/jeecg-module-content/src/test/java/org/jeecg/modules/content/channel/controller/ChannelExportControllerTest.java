package org.jeecg.modules.content.channel.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelExportBiz;
import org.jeecg.modules.content.channel.req.ChannelExportReq;
import org.jeecg.modules.content.channel.service.IChannelExportTaskService;
import org.jeecg.modules.content.channel.vo.ChannelExportTaskVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道导出控制器测试
 * 验证创建导出任务、查询状态的委托行为
 */
@ExtendWith(MockitoExtension.class)
class ChannelExportControllerTest {

    @Mock
    private ChannelExportBiz exportBiz;
    @Mock
    private IChannelExportTaskService exportTaskService;

    @InjectMocks
    private ChannelExportController controller;

    @Test
    void should_create_export_task() {
        ChannelExportReq req = new ChannelExportReq();
        ChannelExportTaskVO vo = ChannelExportTaskVO.builder().taskId("t1").status("pending").build();
        when(exportBiz.createExport(req, "current-user-id")).thenReturn(vo);

        Result<ChannelExportTaskVO> result = controller.createExport(req);

        assertThat(result.isSuccess()).isTrue();
        verify(exportBiz).createExport(req, "current-user-id");
    }

    @Test
    void should_return_export_status() {
        ChannelExportTaskVO vo = ChannelExportTaskVO.builder().taskId("t1").status("completed").build();
        when(exportBiz.getExportStatus("task1")).thenReturn(vo);

        Result<ChannelExportTaskVO> result = controller.getExportStatus("task1");

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void should_error_when_export_task_not_found() {
        when(exportBiz.getExportStatus("nope")).thenReturn(null);

        Result<ChannelExportTaskVO> result = controller.getExportStatus("nope");

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("导出任务不存在");
    }
}
