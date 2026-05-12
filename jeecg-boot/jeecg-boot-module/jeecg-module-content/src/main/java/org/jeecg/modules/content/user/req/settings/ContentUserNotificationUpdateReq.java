package org.jeecg.modules.content.user.req.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区用户通知设置更新请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户通知设置更新请求")
public class ContentUserNotificationUpdateReq {

    @Schema(description = "点赞通知开关", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean likeNoticeEnabled;

    @Schema(description = "评论通知开关", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean commentNoticeEnabled;

    @Schema(description = "关注通知开关", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean followNoticeEnabled;

    @Schema(description = "收藏通知开关", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean favoriteNoticeEnabled;

    @Schema(description = "@通知开关", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean mentionNoticeEnabled;

    @Schema(description = "私信通知开关", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean privateMessageNoticeEnabled;

    @Valid
    @Schema(description = "渠道配置", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private ContentNotificationChannelConfigReq channelConfig;

    @Valid
    @Schema(description = "免打扰规则", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private ContentNotificationDndRuleReq dndRule;
}
