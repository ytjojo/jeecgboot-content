package org.jeecg.modules.content.channel.req.governance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Map;

@Data
@Schema(description = "频道治理操作请求")
public class ChannelGovernanceReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @NotBlank(message = "内容ID不能为空")
    @Schema(description = "内容ID")
    private String contentId;

    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作类型：PIN/UNPIN/FEATURE/UNFEATURE/DELETE/RESTORE/MOVE/EDIT_ASSIST")
    private String action;

    @Schema(description = "目标频道ID（移出频道时使用）")
    private String targetChannelId;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "编辑字段（EDIT_ASSIST 时使用，key=字段名, value=新值）")
    private Map<String, String> editFields;
}
