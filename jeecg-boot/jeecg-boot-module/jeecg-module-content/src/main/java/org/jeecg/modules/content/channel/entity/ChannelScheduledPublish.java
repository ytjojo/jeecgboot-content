package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("channel_scheduled_publish")
@Schema(description = "频道定时发布任务")
public class ChannelScheduledPublish extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "发布者ID")
    private String publisherId;

    @Schema(description = "计划发布时间")
    private Date scheduledTime;

    @Schema(description = "状态：SCHEDULED/PUBLISHED/FAILED/CANCELLED")
    private String publishStatus;

    @Schema(description = "失败原因")
    private String failReason;
}
