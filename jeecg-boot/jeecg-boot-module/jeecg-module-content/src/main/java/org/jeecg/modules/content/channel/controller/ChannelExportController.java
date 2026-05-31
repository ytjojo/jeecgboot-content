package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelExportBiz;
import org.jeecg.modules.content.channel.req.ChannelExportReq;
import org.jeecg.modules.content.channel.vo.ChannelExportTaskVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/jeecg-boot/api/v1/content/channel/export")
@Tag(name = "频道数据导出", description = "频道数据导出接口")
public class ChannelExportController {

    @Resource
    private ChannelExportBiz exportBiz;

    @PostMapping("/create")
    @Operation(summary = "创建导出任务")
    public Result<ChannelExportTaskVO> createExport(@Valid @RequestBody ChannelExportReq req) {
        return Result.OK(exportBiz.createExport(req, getCurrentUserId()));
    }

    @GetMapping("/status")
    @Operation(summary = "查询导出状态")
    public Result<ChannelExportTaskVO> getExportStatus(@RequestParam String taskId) {
        ChannelExportTaskVO vo = exportBiz.getExportStatus(taskId);
        if (vo == null) {
            return Result.error("导出任务不存在");
        }
        return Result.OK(vo);
    }

    private String getCurrentUserId() {
        // TODO: 从安全上下文获取当前用户ID
        return "current-user-id";
    }
}
