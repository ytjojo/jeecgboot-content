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
@TableName("channel_stats")
@Schema(description = "频道统计汇总")
public class ChannelStats {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "统计日期")
    private LocalDate statDate;

    @Schema(description = "统计类型：daily/weekly/monthly")
    private String statType;

    @Schema(description = "订阅数")
    private Integer subscriberCount;

    @Schema(description = "内容数")
    private Integer contentCount;

    @Schema(description = "浏览量")
    private Long pv;

    @Schema(description = "访客数")
    private Long uv;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "评论数")
    private Long commentCount;

    @Schema(description = "收藏数")
    private Long favoriteCount;

    @Schema(description = "分享数")
    private Long shareCount;

    @Schema(description = "有效访问数")
    private Long effectiveVisitCount;

    @Schema(description = "新增订阅数")
    private Integer newSubscriberCount;

    @Schema(description = "流失订阅数")
    private Integer lostSubscriberCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
