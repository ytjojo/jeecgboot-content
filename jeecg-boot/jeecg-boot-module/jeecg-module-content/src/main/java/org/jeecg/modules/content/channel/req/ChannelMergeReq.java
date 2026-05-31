package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "频道合并请求")
public class ChannelMergeReq {

    @NotBlank(message = "源频道ID不能为空")
    @Schema(description = "源频道ID（被合并的频道）", required = true)
    private String sourceChannelId;

    @NotBlank(message = "目标频道ID不能为空")
    @Schema(description = "目标频道ID（合并到的频道）", required = true)
    private String targetChannelId;
}
