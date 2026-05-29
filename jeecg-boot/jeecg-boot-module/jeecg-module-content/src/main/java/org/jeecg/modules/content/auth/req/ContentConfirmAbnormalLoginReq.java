package org.jeecg.modules.content.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 确认异常登录请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "确认异常登录请求")
public class ContentConfirmAbnormalLoginReq {

    @NotBlank(message = "风险事件ID不能为空")
    @Schema(description = "风险事件ID")
    private String eventId;

    @NotNull(message = "是否本人不能为空")
    @Schema(description = "是否本人操作")
    private Boolean isSelf;
}
