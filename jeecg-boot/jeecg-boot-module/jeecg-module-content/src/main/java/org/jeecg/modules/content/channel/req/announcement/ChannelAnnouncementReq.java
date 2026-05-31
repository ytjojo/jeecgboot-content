package org.jeecg.modules.content.channel.req.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "频道公告请求")
public class ChannelAnnouncementReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @NotBlank(message = "公告标题不能为空")
    @Schema(description = "公告标题")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    @Schema(description = "公告内容（富文本）")
    private String content;
}
