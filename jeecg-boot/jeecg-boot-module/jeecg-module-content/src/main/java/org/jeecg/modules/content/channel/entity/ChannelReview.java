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
@TableName("channel_review")
@Schema(description = "频道审核记录")
public class ChannelReview {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "审核ID")
    private String reviewId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "审核类型：create/update_field/archive/merge")
    private String reviewType;

    @Schema(description = "状态：pending/approved/rejected/returned")
    private String status;

    @Schema(description = "申请人ID")
    private String applicantId;

    @Schema(description = "审核人ID")
    private String reviewerId;

    @Schema(description = "审核原因")
    private String reviewReason;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "是否超时：0-否 1-是")
    private Integer timeoutFlag;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
