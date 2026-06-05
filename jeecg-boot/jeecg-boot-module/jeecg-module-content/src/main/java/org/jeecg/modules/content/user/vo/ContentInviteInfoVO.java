package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 邀请码信息视图（用于落地页校验）。
 */
@Data
@Accessors(chain = true)
@Schema(description = "邀请码信息视图")
public class ContentInviteInfoVO {

    @Schema(description = "邀请码是否有效")
    private Boolean valid;

    @Schema(description = "邀请码是否已过期")
    private Boolean expired;

    @Schema(description = "邀请名额是否已满")
    private Boolean maxReached;

    @Schema(description = "邀请人昵称")
    private String inviterNickname;

    @Schema(description = "邀请人头像")
    private String inviterAvatar;

    @Schema(description = "奖励信息说明")
    private String rewardInfo;
}
