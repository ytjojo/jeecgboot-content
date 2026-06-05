package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "删除前置条件校验结果")
public class DeleteCheckResultVO {

    @Schema(description = "是否可删除")
    private boolean canDelete;

    @Schema(description = "阻塞原因列表")
    private List<String> blockReasons;

    @Schema(description = "是否需要组织管理员确认")
    private boolean needOrgAdminConfirm;
}
