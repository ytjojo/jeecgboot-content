package org.jeecg.modules.content.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 内容社区积分明细查询条件。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区积分明细查询条件")
public class ContentUserPointLedgerQueryDTO {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "明细类型：EARN 获取，SPEND 消耗")
    private String type;

    @Schema(description = "开始时间")
    private Date startTime;

    @Schema(description = "结束时间")
    private Date endTime;

    @Schema(description = "当前页，从 1 开始")
    private Integer current;

    @Schema(description = "每页条数，最大 100")
    private Integer size;
}
