package org.jeecg.modules.content.user.req.growth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区功能解锁请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区功能解锁请求")
public class ContentUserFeatureUnlockReq {

    @NotBlank(message = "用户ID不能为空")
    @Size(max = 64, message = "用户ID长度不能超过64位")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String userId;

    @NotBlank(message = "商品ID不能为空")
    @Size(max = 64, message = "商品ID长度不能超过64位")
    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String goodsId;
}
