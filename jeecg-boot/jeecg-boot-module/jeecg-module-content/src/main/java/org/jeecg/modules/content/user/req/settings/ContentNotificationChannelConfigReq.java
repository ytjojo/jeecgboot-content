package org.jeecg.modules.content.user.req.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 内容社区通知渠道配置请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区通知渠道配置请求")
public class ContentNotificationChannelConfigReq {

    @Schema(description = "点赞通知渠道", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private List<@Pattern(regexp = "^(IN_APP|PUSH|EMAIL|SMS)$", message = "点赞通知渠道取值不合法") String> likeChannels;

    @Schema(description = "评论通知渠道", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private List<@Pattern(regexp = "^(IN_APP|PUSH|EMAIL|SMS)$", message = "评论通知渠道取值不合法") String> commentChannels;

    @Schema(description = "关注通知渠道", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private List<@Pattern(regexp = "^(IN_APP|PUSH|EMAIL|SMS)$", message = "关注通知渠道取值不合法") String> followChannels;

    @Schema(description = "收藏通知渠道", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private List<@Pattern(regexp = "^(IN_APP|PUSH|EMAIL|SMS)$", message = "收藏通知渠道取值不合法") String> favoriteChannels;

    @Schema(description = "@通知渠道", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private List<@Pattern(regexp = "^(IN_APP|PUSH|EMAIL|SMS)$", message = "@通知渠道取值不合法") String> mentionChannels;

    @Schema(description = "私信通知渠道", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private List<@Pattern(regexp = "^(IN_APP|PUSH|EMAIL|SMS)$", message = "私信通知渠道取值不合法") String> privateMessageChannels;
}
