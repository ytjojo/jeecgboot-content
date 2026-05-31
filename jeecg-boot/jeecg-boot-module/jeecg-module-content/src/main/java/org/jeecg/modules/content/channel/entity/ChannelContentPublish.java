package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("channel_content_publish")
@Schema(description = "频道内容发布关联")
public class ChannelContentPublish extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "发布者ID")
    private String publisherId;

    @Schema(description = "发布状态：PUBLISHED/PENDING/REJECTED/RECYCLED")
    private String publishStatus;

    @Schema(description = "是否置顶")
    private Boolean isPinned;

    @Schema(description = "置顶排序")
    private Integer pinOrder;

    @Schema(description = "是否精华")
    private Boolean isFeatured;

    @Schema(description = "来源类型：DIRECT/SCHEDULED/MOVE/ADD_EXISTING")
    private String sourceType;
}
