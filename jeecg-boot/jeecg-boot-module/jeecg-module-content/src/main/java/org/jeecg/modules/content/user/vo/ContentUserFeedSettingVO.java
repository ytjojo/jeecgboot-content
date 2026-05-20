package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserFeedSetting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 关注流动态类型设置响应。
 */
@Data
@Accessors(chain = true)
@Schema(description = "关注流动态类型设置")
public class ContentUserFeedSettingVO {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "是否展示发布动态")
    private Boolean publishEnabled;

    @Schema(description = "是否展示点赞动态")
    private Boolean likeEnabled;

    @Schema(description = "是否展示收藏动态")
    private Boolean favoriteEnabled;

    @Schema(description = "启用的动态类型")
    private List<String> activityTypes;

    public static ContentUserFeedSettingVO from(ContentUserFeedSetting setting) {
        if (setting == null) {
            return null;
        }
        return new ContentUserFeedSettingVO()
            .setUserId(setting.getUserId())
            .setPublishEnabled(setting.getPublishEnabled())
            .setLikeEnabled(setting.getLikeEnabled())
            .setFavoriteEnabled(setting.getFavoriteEnabled())
            .setActivityTypes(splitTypes(setting.getActivityTypes()));
    }

    private static List<String> splitTypes(String activityTypes) {
        if (activityTypes == null || activityTypes.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(activityTypes.split(","))
            .map(String::trim)
            .filter(type -> !type.isEmpty())
            .toList();
    }
}
