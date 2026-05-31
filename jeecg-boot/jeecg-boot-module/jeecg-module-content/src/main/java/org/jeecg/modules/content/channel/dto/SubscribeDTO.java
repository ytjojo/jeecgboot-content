package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "订阅请求")
public class SubscribeDTO {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;
}
