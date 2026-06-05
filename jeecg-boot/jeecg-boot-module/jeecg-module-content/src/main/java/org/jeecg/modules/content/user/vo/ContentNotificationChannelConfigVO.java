package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 内容社区通知渠道配置视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区通知渠道配置视图")
public class ContentNotificationChannelConfigVO {

    @Schema(description = "点赞通知渠道")
    private List<String> likeChannels;

    @Schema(description = "评论通知渠道")
    private List<String> commentChannels;

    @Schema(description = "关注通知渠道")
    private List<String> followChannels;

    @Schema(description = "收藏通知渠道")
    private List<String> favoriteChannels;

    @Schema(description = "@通知渠道")
    private List<String> mentionChannels;

    @Schema(description = "私信通知渠道")
    private List<String> privateMessageChannels;

    @Schema(description = "订阅更新通知渠道")
    private List<String> subscriptionChannels;
}
