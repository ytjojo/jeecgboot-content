package org.jeecg.modules.content.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 用户凭证实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_credential")
public class ContentUserCredential extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "凭证类型")
    private String credentialType;

    @Schema(description = "凭证值")
    private String credentialValue;

    @Schema(description = "盐值")
    private String salt;

    @Schema(description = "是否已验证")
    private Boolean verified;

    @Schema(description = "验证时间")
    private Date verifyTime;

    @Schema(description = "状态")
    private String status;
}
