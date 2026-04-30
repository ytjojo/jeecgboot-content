package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserProfile;

import java.util.Date;

/**
 * View object for content user profile.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户资料视图")
public class ContentUserProfileVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String userId;

    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String nickname;

    @Schema(description = "头像", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String avatar;

    @Schema(description = "个人简介", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String bio;

    @Schema(description = "性别", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Integer gender;

    @Schema(description = "生日", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Date birthday;

    @Schema(description = "地区", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String region;

    @Schema(description = "职业", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String profession;

    @Schema(description = "个人链接", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String personalLink;

    @Schema(description = "主页背景图", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String homepageBackground;

    @Schema(description = "主题色", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String themeColor;

    @Schema(description = "主页模块排序JSON", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String moduleOrderJson;

    @Schema(description = "认证类型", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String certificationType;

    @Schema(description = "认证展示文案", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String certificationLabel;

    @Schema(description = "当前状态", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String status;

    @Schema(description = "等级", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Integer level;

    /**
     * Builds the current object from the given request or entity.
     */
    public static ContentUserProfileVO from(ContentUserProfile profile, boolean birthdayVisible) {
        return new ContentUserProfileVO()
            .setUserId(profile.getUserId())
            .setNickname(profile.getNickname())
            .setAvatar(profile.getAvatar())
            .setBio(profile.getBio())
            .setGender(profile.getGender())
            .setBirthday(birthdayVisible ? profile.getBirthday() : null)
            .setRegion(profile.getRegion())
            .setProfession(profile.getProfession())
            .setPersonalLink(profile.getPersonalLink())
            .setHomepageBackground(profile.getHomepageBackground())
            .setThemeColor(profile.getThemeColor())
            .setModuleOrderJson(profile.getModuleOrderJson())
            .setCertificationType(profile.getCertificationType())
            .setCertificationLabel(profile.getCertificationLabel())
            .setStatus(profile.getStatus())
            .setLevel(profile.getLevel());
    }
}
