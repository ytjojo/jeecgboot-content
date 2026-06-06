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
@TableName("content_channel_search_feedback")
@Schema(description = "频道搜索反馈")
public class ContentChannelSearchFeedback {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "反馈的频道ID")
    private String channelId;

    @Schema(description = "反馈动作: click/skip/report")
    private String action;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
