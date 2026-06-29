package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelExportBiz;
import org.jeecg.modules.content.channel.entity.ChannelExportTask;
import org.jeecg.modules.content.channel.req.ChannelExportReq;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.jeecg.modules.content.channel.service.IChannelExportTaskService;
import org.jeecg.modules.content.channel.util.ChannelSecurityUtil;
import org.jeecg.modules.content.channel.vo.ChannelExportTaskVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/v1/content/channel/export")
@Tag(name = "频道数据导出", description = "频道数据导出接口")
public class ChannelExportController {

    @Resource
    private ChannelExportBiz exportBiz;

    @Resource
    private IChannelExportTaskService exportTaskService;

    @Resource
    private ChannelMemberService memberService;

    @PostMapping("/create")
    @Operation(summary = "创建导出任务")
    public Result<ChannelExportTaskVO> createExport(@Valid @RequestBody ChannelExportReq req) {
        String userId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        ChannelSecurityUtil.checkChannelAdminPermission(memberService, req.getChannelId(), userId);
        return Result.OK(exportBiz.createExport(req, userId));
    }

    @GetMapping("/status")
    @Operation(summary = "查询导出状态")
    public Result<ChannelExportTaskVO> getExportStatus(@RequestParam String taskId) {
        String userId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        ChannelExportTask task = exportTaskService.lambdaQuery()
                .eq(ChannelExportTask::getTaskId, taskId)
                .one();
        if (task == null) {
            return Result.error("导出任务不存在");
        }
        ChannelSecurityUtil.checkChannelAdminPermission(memberService, task.getChannelId(), userId);
        return Result.OK(exportBiz.getExportStatus(taskId));
    }

    @GetMapping("/history")
    @Operation(summary = "查询导出历史")
    public Result<Page<ChannelExportTask>> getExportHistory(
            @Parameter(description = "频道ID") @RequestParam(required = false) String channelId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        String userId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        if (channelId != null) {
            ChannelSecurityUtil.checkChannelAdminPermission(memberService, channelId, userId);
        }
        LambdaQueryWrapper<ChannelExportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelExportTask::getUserId, userId);
        if (channelId != null) {
            wrapper.eq(ChannelExportTask::getChannelId, channelId);
        }
        wrapper.orderByDesc(ChannelExportTask::getCreatedTime);
        return Result.OK(exportTaskService.page(new Page<>(current, size), wrapper));
    }

    @GetMapping("/download")
    @Operation(summary = "下载导出文件")
    public void downloadExport(
            @Parameter(description = "任务ID", required = true) @RequestParam String taskId,
            HttpServletResponse response) throws IOException {
        String userId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        ChannelExportTask task = exportTaskService.lambdaQuery()
                .eq(ChannelExportTask::getTaskId, taskId)
                .one();
        if (task == null || !"completed".equals(task.getStatus())) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"导出文件不存在或未完成\"}");
            return;
        }
        ChannelSecurityUtil.checkChannelAdminPermission(memberService, task.getChannelId(), userId);

        File file = new File(task.getFilePath());
        if (!file.exists()) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"导出文件已过期或被删除\"}");
            return;
        }

        String encodedFileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setContentLengthLong(file.length());

        try (InputStream is = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        }
    }
}
