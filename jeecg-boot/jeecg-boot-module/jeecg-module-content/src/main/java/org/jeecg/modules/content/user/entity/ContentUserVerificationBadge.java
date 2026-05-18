package org.jeecg.modules.content.user.entity;

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
 * 内容社区用户认证标识记录。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_verification_badge")
@Schema(description = "内容社区用户认证标识")
public class ContentUserVerificationBadge extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "认证类型")
    private String badgeType;

    @Schema(description = "认证文案")
    private String badgeLabel;

    @Schema(description = "视觉样式编码")
    private String visualStyleKey;

    @Schema(description = "认证描述")
    private String description;

    @Schema(description = "认证元数据JSON")
    private String metadataJson;

    @Schema(description = "状态")
    private String status;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "认证时间")
    private Date verifiedAt;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "过期时间")
    private Date expiresAt;
}
