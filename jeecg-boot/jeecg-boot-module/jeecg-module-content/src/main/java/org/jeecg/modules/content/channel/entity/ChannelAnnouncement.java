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
@TableName("channel_announcement")
@Schema(description = "频道公告")
public class ChannelAnnouncement extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "公告内容（富文本）")
    private String content;

    @Schema(description = "状态：ACTIVE/DELETED")
    private String status;

    @Schema(description = "创建人ID")
    private String createdBy;
}
