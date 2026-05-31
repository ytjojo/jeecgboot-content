package org.jeecg.modules.content.channel.req.publish;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "频道发布请求")
public class ChannelPublishReq {

    @NotBlank(message = "内容ID不能为空")
    @Schema(description = "内容ID")
    private String contentId;

    @NotBlank(message = "内容类型不能为空")
    @Schema(description = "内容类型")
    private String contentType;

    @NotEmpty(message = "目标频道不能为空")
    @Schema(description = "目标频道ID列表")
    private List<String> channelIds;

    @Schema(description = "定时发布时间")
    private java.util.Date scheduledTime;
}
