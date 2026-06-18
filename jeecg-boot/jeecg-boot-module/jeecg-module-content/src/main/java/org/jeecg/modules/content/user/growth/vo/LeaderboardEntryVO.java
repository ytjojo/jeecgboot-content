package org.jeecg.modules.content.user.growth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "排行榜条目")
public class LeaderboardEntryVO {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "得分")
    private Integer score;

    @Schema(description = "排名")
    private Integer rankNum;

    @Schema(description = "是否高亮当前用户")
    private Boolean highlighted;

    @Schema(description = "与上一名得分差值")
    private Integer gap;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像URL")
    private String avatar;
}
