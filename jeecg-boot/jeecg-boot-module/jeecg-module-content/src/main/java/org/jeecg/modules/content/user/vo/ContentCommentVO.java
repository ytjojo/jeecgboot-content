package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * View object for content comment with community role info.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区评论视图")
public class ContentCommentVO {

    @Schema(description = "评论ID")
    private String commentId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "社区角色")
    private String communityRole;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "创建时间")
    private Date createTime;
}
