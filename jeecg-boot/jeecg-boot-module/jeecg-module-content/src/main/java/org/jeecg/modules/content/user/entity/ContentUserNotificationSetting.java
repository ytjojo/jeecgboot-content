package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * Entity for content user notification setting.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_notification_setting")
@Schema(description = "内容社区通知设置")
public class ContentUserNotificationSetting extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "点赞通知开关")
    private Boolean likeNoticeEnabled;

    @Schema(description = "评论通知开关")
    private Boolean commentNoticeEnabled;

    @Schema(description = "关注通知开关")
    private Boolean followNoticeEnabled;

    @Schema(description = "收藏通知开关")
    private Boolean favoriteNoticeEnabled;

    @Schema(description = "@通知开关")
    private Boolean mentionNoticeEnabled;

    @Schema(description = "私信通知开关")
    private Boolean privateMessageNoticeEnabled;

    @Schema(description = "通知渠道JSON")
    private String channelConfigJson;

    @Schema(description = "免打扰规则JSON")
    private String dndRuleJson;

    /**
     * Builds the default configuration for the target user.
     */
    public static ContentUserNotificationSetting defaults(String userId) {
        return new ContentUserNotificationSetting()
            .setUserId(userId)
            .setLikeNoticeEnabled(Boolean.TRUE)
            .setCommentNoticeEnabled(Boolean.TRUE)
            .setFollowNoticeEnabled(Boolean.TRUE)
            .setFavoriteNoticeEnabled(Boolean.TRUE)
            .setMentionNoticeEnabled(Boolean.TRUE)
            .setPrivateMessageNoticeEnabled(Boolean.TRUE)
            .setChannelConfigJson("{}")
            .setDndRuleJson("{}");
    }
}
