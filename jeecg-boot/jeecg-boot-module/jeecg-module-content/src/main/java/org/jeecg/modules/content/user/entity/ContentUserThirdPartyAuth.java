package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 内容社区用户第三方授权记录。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_third_party_auth")
@Schema(description = "内容社区用户第三方授权记录")
public class ContentUserThirdPartyAuth extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "第三方应用名称")
    private String appName;

    @Schema(description = "授权时间")
    private Date authTime;

    @Schema(description = "授权范围(JSON数组)")
    private String scopes;

    @Schema(description = "Access Token 哈希")
    private String tokenHash;

    @Schema(description = "Refresh Token 哈希")
    private String refreshTokenHash;

    @Schema(description = "授权状态: ACTIVE/REVOKED")
    private String status;

    @Schema(description = "撤销时间")
    private Date revokedAt;

    @Schema(description = "第三方开放ID")
    private String openId;

    @Schema(description = "第三方联合ID")
    private String unionId;

    @Schema(description = "第三方昵称")
    private String nickname;

    @Schema(description = "第三方头像")
    private String avatar;

    @Schema(description = "原始授权数据JSON")
    private String rawDataJson;
}
