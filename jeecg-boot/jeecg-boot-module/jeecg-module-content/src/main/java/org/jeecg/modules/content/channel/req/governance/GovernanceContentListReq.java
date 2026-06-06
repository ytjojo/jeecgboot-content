package org.jeecg.modules.content.channel.req.governance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "频道内容列表查询请求")
public class GovernanceContentListReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容类型过滤")
    private String contentType;

    @Schema(description = "发布状态过滤")
    private String publishStatus;

    @Schema(description = "是否置顶过滤")
    private Boolean isPinned;

    @Schema(description = "是否精华过滤")
    private Boolean isFeatured;

    @Schema(description = "关键词搜索")
    private String keyword;

    @Schema(description = "当前页码", defaultValue = "1")
    private Integer current = 1;

    @Schema(description = "每页条数", defaultValue = "10")
    private Integer size = 10;

}
