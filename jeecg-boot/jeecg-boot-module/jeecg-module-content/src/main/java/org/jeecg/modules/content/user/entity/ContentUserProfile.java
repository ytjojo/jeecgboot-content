package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Entity for content user profile.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_profile")
@Schema(description = "内容社区用户资料")
public class ContentUserProfile extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "性别")
    private Integer gender;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "生日")
    private Date birthday;

    @Schema(description = "地区")
    private String region;

    @Schema(description = "职业")
    private String profession;

    @Schema(description = "个人链接")
    private String personalLink;

    @Schema(description = "主页背景图")
    private String homepageBackground;

    @Schema(description = "主题色")
    private String themeColor;

    @Schema(description = "主页模块排序JSON")
    private String moduleOrderJson;

    @Schema(description = "认证类型")
    private String certificationType;

    @Schema(description = "认证展示文案")
    private String certificationLabel;

    @Schema(description = "昵称历史JSON")
    private String nicknameHistoryJson;

    @Schema(description = "头像历史JSON")
    private String avatarHistoryJson;

    @Schema(description = "资料完善状态")
    private String profileCompletionState;

    @Schema(description = "资料审核状态")
    private String profileReviewStatus;

    @Schema(description = "资料版本号")
    private Integer profileVersion;

    @Schema(description = "当前用户状态")
    private String status;

    @Schema(description = "等级")
    private Integer level;

    @Schema(description = "积分余额")
    private Integer pointBalance;

    @Schema(description = "成长值")
    private Integer growthValue;

    @TableField(exist = false)
    private String birthdayVisibility;
}
