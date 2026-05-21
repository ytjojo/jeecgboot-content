package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserBlock;
import org.jeecg.modules.content.user.entity.ContentUserProfile;

import java.util.Date;

/**
 * 内容社区黑名单用户列表项。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区黑名单用户列表项")
public class ContentUserBlacklistItemVO {

    @Schema(description = "被拉黑用户ID")
    private String blockedUserId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "拉黑时间")
    private Date blockTime;

    /**
     * 组装拉黑关系与用户展示字段。
     */
    public static ContentUserBlacklistItemVO from(ContentUserBlock block, ContentUserProfile profile) {
        ContentUserBlacklistItemVO item = new ContentUserBlacklistItemVO()
            .setBlockedUserId(block.getBlockedUserId())
            .setBlockTime(block.getBlockTime());
        if (profile != null) {
            item.setNickname(profile.getNickname())
                .setAvatar(profile.getAvatar());
        }
        return item;
    }
}
