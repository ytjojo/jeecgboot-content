package org.jeecg.modules.content.user.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserProfile;

import java.util.Date;

@Data
@Accessors(chain = true)
public class ContentUserProfileVO {

    private String userId;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer gender;
    private Date birthday;
    private String region;
    private String profession;
    private String personalLink;
    private String homepageBackground;
    private String themeColor;
    private String moduleOrderJson;
    private String certificationType;
    private String certificationLabel;
    private String status;
    private Integer level;

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
