package org.jeecg.modules.content.user.req.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Request model for content user privacy update.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户隐私更新请求")
public class ContentUserPrivacyUpdateReq {

    @Pattern(
        regexp = "^(PUBLIC|FOLLOWERS_ONLY|MUTUAL_ONLY|PRIVATE)$",
        message = "生日可见范围取值不合法"
    )
    @Schema(description = "生日可见范围", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String birthdayVisibility;

    @Pattern(
        regexp = "^(PUBLIC|FOLLOWERS_ONLY|MUTUAL_ONLY|PRIVATE)$",
        message = "性别可见范围取值不合法"
    )
    @Schema(description = "性别可见范围", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String genderVisibility;

    @Pattern(
        regexp = "^(PUBLIC|FOLLOWERS_ONLY|MUTUAL_ONLY|PRIVATE)$",
        message = "地区可见范围取值不合法"
    )
    @Schema(description = "地区可见范围", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String regionVisibility;

    @Pattern(
        regexp = "^(PUBLIC|FOLLOWERS_ONLY|MUTUAL_ONLY|PRIVATE)$",
        message = "职业可见范围取值不合法"
    )
    @Schema(description = "职业可见范围", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String professionVisibility;

    @Pattern(
        regexp = "^(PUBLIC|FOLLOWERS_ONLY|MUTUAL_ONLY|PRIVATE)$",
        message = "个人链接可见范围取值不合法"
    )
    @Size(max = 32, message = "个人链接可见范围长度不能超过32位")
    @Schema(description = "个人链接可见范围", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String personalLinkVisibility;

    @Pattern(
        regexp = "^(PUBLIC|FOLLOWERS_ONLY|MUTUAL_ONLY|PRIVATE)$",
        message = "认证标识可见范围取值不合法"
    )
    @Size(max = 32, message = "认证标识可见范围长度不能超过32位")
    @Schema(description = "认证标识可见范围", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String verificationBadgeVisibility;

    @Pattern(
        regexp = "^(PUBLIC|FOLLOWERS_ONLY|MUTUAL_ONLY|PRIVATE)$",
        message = "绑定标识可见范围取值不合法"
    )
    @Size(max = 32, message = "绑定标识可见范围长度不能超过32位")
    @Schema(description = "绑定标识可见范围", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String contactBadgeVisibility;

    @Pattern(
        regexp = "^(PUBLIC|FOLLOWERS_ONLY|MUTUAL_ONLY|PRIVATE)$",
        message = "主页可见范围取值不合法"
    )
    @Schema(description = "主页可见范围", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String homepageVisibility;

    @Pattern(
        regexp = "^(PUBLIC|FOLLOWERS_ONLY|MUTUAL_ONLY|PRIVATE)$",
        message = "动态可见范围取值不合法"
    )
    @Schema(description = "动态可见范围", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String dynamicVisibility;

    @Schema(description = "是否展示在线状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean onlineStatusVisible;

    @Schema(description = "是否允许搜索引擎收录", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean allowSearchEngineIndex;

    @Schema(description = "是否允许用户搜索", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean allowUserSearch;
}
