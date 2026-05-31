package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "移除成员请求")
public class RemoveMemberDTO {

    @NotBlank(message = "成员ID不能为空")
    @Schema(description = "成员ID")
    private String memberId;

    @Schema(description = "移除原因")
    private String reason;
}
