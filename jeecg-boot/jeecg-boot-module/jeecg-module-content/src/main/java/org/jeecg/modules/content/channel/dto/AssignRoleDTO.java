package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "分配角色请求")
public class AssignRoleDTO {

    @NotBlank(message = "成员ID不能为空")
    @Schema(description = "成员ID")
    private String memberId;

    @NotNull(message = "角色不能为空")
    @Schema(description = "角色: 1=频道主, 2=管理员, 3=内容编辑, 4=普通成员")
    private Integer role;
}
