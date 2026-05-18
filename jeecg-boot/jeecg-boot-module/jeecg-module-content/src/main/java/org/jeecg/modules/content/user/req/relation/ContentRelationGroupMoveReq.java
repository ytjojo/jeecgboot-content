package org.jeecg.modules.content.user.req.relation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 内容社区关注对象移动分组请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区关注对象移动分组请求")
public class ContentRelationGroupMoveReq {

    @NotEmpty(message = "目标用户ID列表不能为空")
    @Size(max = 100, message = "目标用户ID数量不能超过100个")
    @Schema(description = "目标用户ID列表", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private List<@NotBlank(message = "目标用户ID不能为空") @Size(max = 64, message = "目标用户ID长度不能超过64位") String> targetUserIds;

    @NotBlank(message = "关系分组ID不能为空")
    @Size(max = 64, message = "关系分组ID长度不能超过64位")
    @Schema(description = "关系分组ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String relationGroupId;
}
