package org.jeecg.modules.content.user.req.growth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Request model for content point adjust.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区积分与成长调整请求")
public class ContentPointAdjustReq {

    @NotBlank(message = "用户ID不能为空")
    @Size(max = 64, message = "用户ID长度不能超过64位")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String userId;

    @NotBlank(message = "来源类型不能为空")
    @Size(max = 32, message = "来源类型长度不能超过32位")
    @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String sourceType;

    @Schema(description = "积分变动量", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Integer pointDelta;

    @Schema(description = "成长值变动量", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Integer growthDelta;

    @Size(max = 64, message = "业务ID长度不能超过64位")
    @Schema(description = "业务ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String bizId;
}
