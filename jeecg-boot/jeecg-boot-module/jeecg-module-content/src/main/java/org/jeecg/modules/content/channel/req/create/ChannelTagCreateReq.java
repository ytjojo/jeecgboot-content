package org.jeecg.modules.content.channel.req.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建标签请求")
public class ChannelTagCreateReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 20, message = "标签名称不能超过20个字符")
    @Schema(description = "标签名称")
    private String name;
}
