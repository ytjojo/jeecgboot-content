package org.jeecg.modules.content.channel.req.publish;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "将已发布内容添加到频道请求")
public class ChannelAddExistingContentReq {

    @NotBlank(message = "内容ID不能为空")
    @Schema(description = "内容ID")
    private String contentId;

    @NotBlank(message = "内容类型不能为空")
    @Schema(description = "内容类型")
    private String contentType;

    @NotEmpty(message = "目标频道不能为空")
    @Schema(description = "目标频道ID列表")
    private List<String> channelIds;

    @Schema(description = "添加原因")
    private String reason;
}
