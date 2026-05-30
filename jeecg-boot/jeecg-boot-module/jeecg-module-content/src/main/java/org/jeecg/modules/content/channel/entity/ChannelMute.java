package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("content_channel_mute")
@Schema(description = "禁言记录表")
public class ChannelMute {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "禁言原因")
    private String reason;

    @Schema(description = "开始时间")
    private Date startTime;

    @Schema(description = "结束时间，NULL表示永久禁言")
    private Date endTime;

    @Schema(description = "解除方式: 1=自动到期 2=手动解除")
    private Integer unmuteType;

    @Schema(description = "解除时间")
    private Date unmuteTime;

    @Schema(description = "创建时间")
    private Date createTime;
}
