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
@TableName("content_channel_governance_log")
@Schema(description = "治理操作日志表")
public class ChannelGovernanceLog {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "操作类型: 1=移除 2=禁言 3=解除禁言 4=加入黑名单 5=移出黑名单")
    private Integer action;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "目标用户ID")
    private String targetUserId;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "扩展数据JSON")
    private String extraData;

    @Schema(description = "创建时间")
    private Date createTime;
}
