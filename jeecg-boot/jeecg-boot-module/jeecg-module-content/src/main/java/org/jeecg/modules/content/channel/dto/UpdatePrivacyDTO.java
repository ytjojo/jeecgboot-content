package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新隐私设置请求")
public class UpdatePrivacyDTO {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @NotNull(message = "隐私类型不能为空")
    @Schema(description = "隐私类型: 1=公开, 2=私有")
    private Integer privacyType;
}
