package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("channel_appeal")
@Schema(description = "频道申诉记录")
public class ChannelAppeal {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "申诉ID")
    private String appealId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "关联的生命周期日志ID")
    private String lifecycleLogId;

    @Schema(description = "申诉人ID")
    private String applicantId;

    @Schema(description = "申诉理由")
    private String appealReason;

    @Schema(description = "附件URL(JSON数组)")
    private String attachmentUrls;

    @Schema(description = "状态：pending/processing/approved/rejected")
    private String status;

    @Schema(description = "处理人ID")
    private String handlerId;

    @Schema(description = "处理结果")
    private String handleResult;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "首次响应时间")
    private LocalDateTime firstResponseTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
