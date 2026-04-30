package org.jeecg.modules.content.user.req.relation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Request model for content follow.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区关注请求")
public class ContentFollowReq {

    @NotBlank(message = "目标用户ID不能为空")
    @Size(max = 64, message = "目标用户ID长度不能超过64位")
    @Schema(description = "目标用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String targetUserId;

    @Size(max = 64, message = "关系分组ID长度不能超过64位")
    @Schema(description = "关系分组ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String relationGroupId;
}
