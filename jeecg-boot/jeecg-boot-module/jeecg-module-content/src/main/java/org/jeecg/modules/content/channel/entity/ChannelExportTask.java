package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("channel_export_task")
@Schema(description = "频道导出任务")
public class ChannelExportTask {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "导出用户ID")
    private String userId;

    @Schema(description = "导出类型：core_stats/interaction/user_analysis")
    private String exportType;

    @Schema(description = "文件格式：xlsx/csv")
    private String fileFormat;

    @Schema(description = "状态：pending/processing/completed/failed")
    private String status;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "数据行数")
    private Integer rowCount;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "文件过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
