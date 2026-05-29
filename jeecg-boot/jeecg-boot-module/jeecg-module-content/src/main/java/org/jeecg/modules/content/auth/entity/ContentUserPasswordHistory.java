package org.jeecg.modules.content.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 用户密码历史实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_password_history")
public class ContentUserPasswordHistory extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "密码哈希")
    private String passwordHash;

    @Schema(description = "盐值")
    private String salt;
}
