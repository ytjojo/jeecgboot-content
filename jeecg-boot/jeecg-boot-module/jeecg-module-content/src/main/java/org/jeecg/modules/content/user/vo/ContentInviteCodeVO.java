package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区邀请码视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区邀请码视图")
public class ContentInviteCodeVO {

    @Schema(description = "邀请码")
    private String inviteCode;

    @Schema(description = "邀请链接")
    private String inviteUrl;
}
