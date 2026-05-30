package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.channel.enums.ReviewResult;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("content_channel_review")
@Schema(description = "频道审核记录表")
public class ChannelReview {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "审核记录ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "审核人ID")
    private String reviewerId;

    @Schema(description = "审核结果: 1=Pass, 2=Reject, 3=ReturnForEdit")
    private ReviewResult result;

    @Schema(description = "审核原因")
    private String reason;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private Date createTime;
}
