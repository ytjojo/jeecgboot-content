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
@TableName("channel_lifecycle_log")
@Schema(description = "频道生命周期变更日志")
public class ChannelLifecycleLog {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "日志ID")
    private String logId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "操作类型：freeze/unfreeze/hide/restrict_recommend/close/archive/merge/delete")
    private String actionType;

    @Schema(description = "变更前状态")
    private String fromStatus;

    @Schema(description = "变更后状态")
    private String toStatus;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "影响范围")
    private String impactScope;

    @Schema(description = "目标频道ID(合并时)")
    private String targetChannelId;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
