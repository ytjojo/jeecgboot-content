package org.jeecg.modules.content.user.req.growth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 内容社区勋章佩戴保存请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区勋章佩戴保存请求")
public class ContentUserBadgeWearReq {

    @NotEmpty(message = "勋章授予ID列表不能为空")
    @Size(max = 5, message = "最多佩戴5个勋章")
    @Schema(description = "勋章授予ID列表", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private List<@NotBlank(message = "勋章授予ID不能为空") @Size(max = 64, message = "勋章授予ID长度不能超过64位") String> grantIds;
}
