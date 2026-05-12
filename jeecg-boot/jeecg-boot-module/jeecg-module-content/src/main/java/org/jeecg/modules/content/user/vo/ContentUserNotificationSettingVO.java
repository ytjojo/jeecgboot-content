package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;

/**
 * 内容社区用户通知设置视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户通知设置视图")
public class ContentUserNotificationSettingVO {

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

    @Schema(description = "渠道配置")
    private ContentNotificationChannelConfigVO channelConfig;

    @Schema(description = "免打扰规则")
    private ContentNotificationDndRuleVO dndRule;

    /**
     * 从通知设置实体构建视图对象。
     */
    public static ContentUserNotificationSettingVO from(ContentUserNotificationSetting setting,
                                                       ContentNotificationChannelConfigVO channelConfig,
                                                       ContentNotificationDndRuleVO dndRule) {
        return new ContentUserNotificationSettingVO()
            .setUserId(setting.getUserId())
            .setLikeNoticeEnabled(setting.getLikeNoticeEnabled())
            .setCommentNoticeEnabled(setting.getCommentNoticeEnabled())
            .setFollowNoticeEnabled(setting.getFollowNoticeEnabled())
            .setFavoriteNoticeEnabled(setting.getFavoriteNoticeEnabled())
            .setMentionNoticeEnabled(setting.getMentionNoticeEnabled())
            .setPrivateMessageNoticeEnabled(setting.getPrivateMessageNoticeEnabled())
            .setChannelConfig(channelConfig)
            .setDndRule(dndRule);
    }
}
