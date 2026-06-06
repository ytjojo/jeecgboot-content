package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "隐私设置更新请求")
public class UpdatePrivacyReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID", required = true)
    private String channelId;

    @NotNull(message = "隐私设置不能为空")
    @Schema(description = "隐私设置: 1=公开 2=私有", required = true)
    private Integer privacy;
}
