package org.jeecg.modules.content.user.req.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Request model for content user profile update.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户资料更新请求")
public class ContentUserProfileUpdateReq {

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
}
