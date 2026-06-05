package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "频道列表查询参数")
public class ChannelListQuery {

    @Schema(description = "频道类型: system/personal/organization")
    private String channelType;

    @Schema(description = "审核状态: DRAFT/PENDING_REVIEW/ACTIVE/REJECTED/DELETE_COOLING/DELETED")
    private String status;

    @Schema(description = "关键词（模糊搜索名称）")
    private String keyword;
}
