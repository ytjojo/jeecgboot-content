package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 风险事件申诉请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "风险事件申诉请求")
public class ContentRiskAppealReq {

    @NotBlank(message = "风险事件ID不能为空")
    @Schema(description = "风险事件ID")
    private String eventId;

    @Schema(description = "处理备注")
    private String note;
}
