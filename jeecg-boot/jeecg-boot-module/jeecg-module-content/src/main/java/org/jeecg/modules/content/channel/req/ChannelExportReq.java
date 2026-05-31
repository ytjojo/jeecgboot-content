package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "频道数据导出请求")
public class ChannelExportReq {

    @Schema(description = "频道ID", required = true)
    private String channelId;

    @Schema(description = "导出类型：core_stats/interaction/user_analysis", required = true)
    private String exportType;

    @Schema(description = "文件格式：xlsx/csv")
    private String fileFormat;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;
}
