package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * Entity for content user badge definition.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_badge_definition")
public class ContentUserBadgeDefinition extends JeecgEntity {

    @Schema(description = "勋章编码")
    private String badgeCode;

    @Schema(description = "勋章名称")
    private String badgeName;

    @Schema(description = "勋章类型")
    private String badgeType;

    @Schema(description = "发放规则配置JSON")
    private String ruleConfigJson;

    @Schema(description = "有效期天数")
    private Integer validDays;

    @Schema(description = "是否自动发放")
    private Boolean autoGrant;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
