package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "禁言成员请求")
public class MuteMemberDTO {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private String userId;

    @NotNull(message = "禁言天数不能为空")
    @Schema(description = "禁言天数, 0表示永久")
    private Integer days;

    @Schema(description = "禁言原因")
    private String reason;
}
