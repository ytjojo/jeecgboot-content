package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "黑名单操作请求")
public class BlacklistDTO {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "操作原因")
    private String reason;
}
