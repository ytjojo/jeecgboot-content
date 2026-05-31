package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "频道导出任务VO")
public class ChannelExportTaskVO {

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "数据行数")
    private Integer rowCount;

    @Schema(description = "文件过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "错误信息")
    private String errorMessage;
}
