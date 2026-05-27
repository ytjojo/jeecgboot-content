package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区邀请统计视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区邀请统计")
public class ContentInviteStatsVO {

    @Schema(description = "总邀请人数")
    private Long totalInvites;

    @Schema(description = "成功注册人数")
    private Long successfulRegistrations;

    @Schema(description = "累计获得积分")
    private Integer totalPointsEarned;
}
