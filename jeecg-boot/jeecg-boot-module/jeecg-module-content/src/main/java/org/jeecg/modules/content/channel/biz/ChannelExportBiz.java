package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.entity.ChannelExportTask;
import org.jeecg.modules.content.channel.req.ChannelExportReq;
import org.jeecg.modules.content.channel.service.IChannelExportTaskService;
import org.jeecg.modules.content.channel.vo.ChannelExportTaskVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

@Service
public class ChannelExportBiz {

    @Resource
    private IChannelExportTaskService exportTaskService;

    public ChannelExportTaskVO createExport(ChannelExportReq req, String userId) {
        String taskId = UUID.randomUUID().toString();
        ChannelExportTask task = new ChannelExportTask()
                .setTaskId(taskId)
                .setChannelId(req.getChannelId())
                .setUserId(userId)
                .setExportType(req.getExportType())
                .setFileFormat(req.getFileFormat())
                .setStartDate(req.getStartDate())
                .setEndDate(req.getEndDate())
                .setStatus("pending");
        exportTaskService.save(task);

        return ChannelExportTaskVO.builder()
                .taskId(taskId)
                .status("pending")
                .build();
    }

    public ChannelExportTaskVO getExportStatus(String taskId) {
        ChannelExportTask task = exportTaskService.lambdaQuery()
                .eq(ChannelExportTask::getTaskId, taskId)
                .one();
        if (task == null) {
            return null;
        }
        return ChannelExportTaskVO.builder()
                .taskId(task.getTaskId())
                .status(task.getStatus())
                .rowCount(task.getRowCount())
                .expireTime(task.getExpireTime())
                .errorMessage(task.getErrorMessage())
                .build();
    }
}
