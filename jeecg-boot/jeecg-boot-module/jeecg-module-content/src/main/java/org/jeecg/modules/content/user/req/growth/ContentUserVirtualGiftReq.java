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
 * 内容社区虚拟礼物赠送请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区虚拟礼物赠送请求")
public class ContentUserVirtualGiftReq {

    @NotBlank(message = "赠送人ID不能为空")
    @Size(max = 64, message = "赠送人ID长度不能超过64位")
    @Schema(description = "赠送人ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String senderUserId;

    @NotBlank(message = "接收人ID不能为空")
    @Size(max = 64, message = "接收人ID长度不能超过64位")
    @Schema(description = "接收人ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String receiverUserId;

    @NotBlank(message = "礼物商品ID不能为空")
    @Size(max = 64, message = "礼物商品ID长度不能超过64位")
    @Schema(description = "礼物商品ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String giftGoodsId;

    @NotNull(message = "赠送数量不能为空")
    @Min(value = 1, message = "赠送数量不能小于1")
    @Max(value = 999, message = "赠送数量不能超过999")
    @Schema(description = "赠送数量", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Integer quantity;

    @Size(max = 255, message = "赠言长度不能超过255位")
    @Schema(description = "赠言", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String message;
}
