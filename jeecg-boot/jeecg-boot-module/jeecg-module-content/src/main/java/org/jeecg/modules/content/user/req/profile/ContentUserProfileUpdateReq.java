package org.jeecg.modules.content.user.req.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称长度不能超过20位")
    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String nickname;

    @NotBlank(message = "头像不能为空")
    @Size(max = 500, message = "头像长度不能超过500位")
    @Schema(description = "头像", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String avatar;

    @Size(max = 500, message = "个人简介长度不能超过500位")
    @Schema(description = "个人简介", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String bio;

    @Schema(description = "性别", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Integer gender;

    @Schema(description = "生日", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Date birthday;

    @Size(max = 64, message = "地区长度不能超过64位")
    @Schema(description = "地区", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String region;

    @Size(max = 64, message = "职业长度不能超过64位")
    @Schema(description = "职业", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String profession;

    @Size(max = 255, message = "个人链接长度不能超过255位")
    @Pattern(regexp = "^(https?://|/).*$", message = "个人链接格式不合法")
    @Schema(description = "个人链接", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String personalLink;

    @Size(max = 500, message = "主页背景图长度不能超过500位")
    @Schema(description = "主页背景图", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String homepageBackground;

    @Size(max = 32, message = "主题色长度不能超过32位")
    @Schema(description = "主题色", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String themeColor;

    @Size(max = 2000, message = "主页模块排序JSON长度不能超过2000位")
    @Schema(description = "主页模块排序JSON", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String moduleOrderJson;

    @Size(max = 64, message = "认证类型长度不能超过64位")
    @Schema(description = "认证类型", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String certificationType;

    @Size(max = 64, message = "认证展示文案长度不能超过64位")
    @Schema(description = "认证展示文案", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String certificationLabel;
}
