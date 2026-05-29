package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 注销申请请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "注销申请请求")
public class ContentCancelApplyReq {

    @Schema(description = "用户ID（由认证上下文自动填充）")
    private String userId;

    @Schema(description = "注销原因")
    private String reason;

    @Min(value = 7, message = "冷静期最少7天")
    @Max(value = 30, message = "冷静期最多30天")
    @Schema(description = "冷静期天数(7-30)")
    private Integer cooldownDays;
}
