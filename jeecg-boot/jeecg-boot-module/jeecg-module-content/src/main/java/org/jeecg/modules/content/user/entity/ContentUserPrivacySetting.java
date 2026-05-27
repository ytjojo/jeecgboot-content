package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * Entity for content user privacy setting.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_privacy_setting")
@Schema(description = "内容社区用户隐私设置")
public class ContentUserPrivacySetting extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "生日可见范围")
    private String birthdayVisibility;

    @Schema(description = "性别可见范围")
    private String genderVisibility;

    @Schema(description = "地区可见范围")
    private String regionVisibility;

    @Schema(description = "职业可见范围")
    private String professionVisibility;

    @Schema(description = "个人链接可见范围")
    private String personalLinkVisibility;

    @Schema(description = "认证标识可见范围")
    private String verificationBadgeVisibility;

    @Schema(description = "绑定标识可见范围")
    private String contactBadgeVisibility;

    @Schema(description = "主页可见范围")
    private String homepageVisibility;

    @Schema(description = "动态可见范围")
    private String dynamicVisibility;

    @Schema(description = "在线状态是否可见（旧字段，兼容保留）")
    private Boolean onlineStatusVisible;

    @Schema(description = "在线状态可见性：PUBLIC/HIDDEN/MUTUAL_ONLY")
    private String onlineStatusVisibility;

    @Schema(description = "浏览历史可见范围")
    private String browseHistoryVisibility;

    @Schema(description = "点赞活动可见范围")
    private String likeActivityVisibility;

    @Schema(description = "收藏可见范围")
    private String favoriteVisibility;

    @Schema(description = "是否允许搜索引擎索引")
    private Boolean allowSearchEngineIndex;

    @Schema(description = "是否允许用户搜索")
    private Boolean allowUserSearch;
}
