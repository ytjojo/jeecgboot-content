package org.jeecg.modules.content.user.req.growth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区积分兑换请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区积分兑换请求")
public class ContentUserExchangeReq {

    @NotBlank(message = "用户ID不能为空")
    @Size(max = 64, message = "用户ID长度不能超过64位")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String userId;

    @NotBlank(message = "商品ID不能为空")
    @Size(max = 64, message = "商品ID长度不能超过64位")
    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String goodsId;

    @NotNull(message = "兑换数量不能为空")
    @Min(value = 1, message = "兑换数量不能小于1")
    @Max(value = 999, message = "兑换数量不能超过999")
    @Schema(description = "兑换数量", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Integer quantity;

    @Size(max = 64, message = "requestId长度不能超过64位")
    @Schema(description = "请求幂等ID，相同ID的重复请求返回已有结果")
    private String requestId;
}
