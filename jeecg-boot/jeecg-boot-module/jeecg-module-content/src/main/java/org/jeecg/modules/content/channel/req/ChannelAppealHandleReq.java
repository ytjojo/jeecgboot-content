package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "频道申诉处理请求")
public class ChannelAppealHandleReq {

    @NotBlank(message = "申诉ID不能为空")
    @Schema(description = "申诉ID", required = true)
    private String appealId;

    @NotBlank(message = "处理结果不能为空")
    @Schema(description = "处理结果：approved/rejected", required = true)
    private String action;

    @Schema(description = "处理说明")
    private String handleResult;
}
