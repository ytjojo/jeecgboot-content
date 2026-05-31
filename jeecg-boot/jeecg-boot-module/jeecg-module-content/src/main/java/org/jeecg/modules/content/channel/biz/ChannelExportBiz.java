package org.jeecg.modules.content.channel.biz;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeecg.modules.content.channel.entity.ChannelExportTask;
import org.jeecg.modules.content.channel.req.ChannelExportReq;
import org.jeecg.modules.content.channel.service.IChannelExportTaskService;
import org.jeecg.modules.content.channel.vo.ChannelExportTaskVO;
import org.jeecg.modules.content.channel.vo.ChannelStatsVO;
import org.jeecg.modules.content.channel.vo.ChannelTrendVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ChannelExportBiz {

    private static final int EXPIRE_DAYS = 7;

    @Value("${jeecg.path.upload:/tmp/upload}")
    private String uploadPath;

    @Resource
    private IChannelExportTaskService exportTaskService;

    @Resource
    private ChannelStatsBiz channelStatsBiz;

    public ChannelExportTaskVO createExport(ChannelExportReq req, String userId) {
        String fileFormat = req.getFileFormat() != null ? req.getFileFormat() : "xlsx";
        ChannelExportTask task = new ChannelExportTask()
                .setChannelId(req.getChannelId())
                .setUserId(userId)
                .setExportType(req.getExportType())
                .setFileFormat(fileFormat)
                .setStartDate(req.getStartDate())
                .setEndDate(req.getEndDate())
                .setStatus("pending");
        exportTaskService.save(task);
        task.setTaskId(task.getId());
        exportTaskService.updateById(task);

        // 异步处理导出
        processExport(task.getId());

        return ChannelExportTaskVO.builder()
                .taskId(task.getTaskId())
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

    public void processExport(String taskId) {
        ChannelExportTask task = exportTaskService.getById(taskId);
        if (task == null) {
            return;
        }

        try {
            task.setStatus("processing");
            task.setUpdatedTime(LocalDateTime.now());
            exportTaskService.updateById(task);

            String exportDir = uploadPath + "/channel-export/";
            File dir = new File(exportDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = task.getExportType() + "_" + task.getChannelId() + "_"
                    + UUID.randomUUID().toString().substring(0, 8);
            int rowCount;

            if ("csv".equals(task.getFileFormat())) {
                String filePath = exportDir + fileName + ".csv";
                rowCount = generateCsv(task, filePath);
                task.setFilePath(filePath);
            } else {
                String filePath = exportDir + fileName + ".xlsx";
                rowCount = generateExcel(task, filePath);
                task.setFilePath(filePath);
            }

            File file = new File(task.getFilePath());
            task.setFileSize(file.length());
            task.setRowCount(rowCount);
            task.setStatus("completed");
            task.setExpireTime(LocalDateTime.now().plusDays(EXPIRE_DAYS));
            task.setUpdatedTime(LocalDateTime.now());
            exportTaskService.updateById(task);

            log.info("导出任务完成: taskId={}, filePath={}, rows={}", task.getTaskId(), task.getFilePath(), rowCount);
        } catch (Exception e) {
            log.error("导出任务失败: taskId={}", task.getTaskId(), e);
            task.setStatus("failed");
            task.setErrorMessage(e.getMessage());
            task.setUpdatedTime(LocalDateTime.now());
            exportTaskService.updateById(task);
        }
    }

    private int generateExcel(ChannelExportTask task, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(task.getExportType());

            List<String[]> data = buildExportData(task);
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i);
                String[] rowData = data.get(i);
                for (int j = 0; j < rowData.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(rowData[j]);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            return Math.max(0, data.size() - 1); // 减去表头行
        }
    }

    private int generateCsv(ChannelExportTask task, String filePath) throws IOException {
        List<String[]> data = buildExportData(task);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            for (String[] row : data) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
        }
        return Math.max(0, data.size() - 1);
    }

    private List<String[]> buildExportData(ChannelExportTask task) {
        return switch (task.getExportType()) {
            case "core_stats" -> buildCoreStatsData(task);
            case "interaction" -> buildInteractionData(task);
            case "user_analysis" -> buildUserAnalysisData(task);
            default -> throw new IllegalArgumentException("不支持的导出类型: " + task.getExportType());
        };
    }

    private List<String[]> buildCoreStatsData(ChannelExportTask task) {
        ChannelStatsVO stats = channelStatsBiz.getCoreStats(task.getChannelId());
        java.util.ArrayList<String[]> rows = new java.util.ArrayList<>();
        rows.add(new String[]{"频道ID", "订阅数", "内容数", "PV", "UV", "点赞数", "评论数", "收藏数", "分享数"});
        rows.add(new String[]{
                stats.getChannelId(),
                String.valueOf(stats.getSubscriberCount()),
                String.valueOf(stats.getContentCount()),
                String.valueOf(stats.getPv()),
                String.valueOf(stats.getUv()),
                String.valueOf(stats.getLikeCount()),
                String.valueOf(stats.getCommentCount()),
                String.valueOf(stats.getFavoriteCount()),
                String.valueOf(stats.getShareCount())
        });
        return rows;
    }

    private List<String[]> buildInteractionData(ChannelExportTask task) {
        ChannelTrendVO trend = channelStatsBiz.getTrendData(
                task.getChannelId(), task.getStartDate(), task.getEndDate(), "daily");
        java.util.ArrayList<String[]> rows = new java.util.ArrayList<>();
        rows.add(new String[]{"日期", "订阅数", "内容数", "PV", "UV"});
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < trend.getDates().size(); i++) {
            rows.add(new String[]{
                    trend.getDates().get(i) != null ? trend.getDates().get(i).format(fmt) : "",
                    String.valueOf(trend.getSubscriberCounts().get(i)),
                    String.valueOf(trend.getContentCounts().get(i)),
                    String.valueOf(trend.getPvs().get(i)),
                    String.valueOf(trend.getUvs().get(i))
            });
        }
        return rows;
    }

    private List<String[]> buildUserAnalysisData(ChannelExportTask task) {
        ChannelStatsVO stats = channelStatsBiz.getCoreStats(task.getChannelId());
        java.util.ArrayList<String[]> rows = new java.util.ArrayList<>();
        rows.add(new String[]{"频道ID", "订阅数", "数据更新时间"});
        rows.add(new String[]{
                stats.getChannelId(),
                String.valueOf(stats.getSubscriberCount()),
                stats.getDataUpdateTime() != null ? stats.getDataUpdateTime().toString() : ""
        });
        return rows;
    }
}
