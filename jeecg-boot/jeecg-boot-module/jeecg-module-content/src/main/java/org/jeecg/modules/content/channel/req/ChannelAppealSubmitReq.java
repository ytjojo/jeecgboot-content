package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "频道申诉提交请求")
public class ChannelAppealSubmitReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID", required = true)
    private String channelId;

    @Schema(description = "关联的生命周期日志ID")
    private String lifecycleLogId;

    @NotBlank(message = "申诉理由不能为空")
    @Schema(description = "申诉理由", required = true)
    private String appealReason;

    @Schema(description = "附件URL(JSON数组)")
    private String attachmentUrls;
}
