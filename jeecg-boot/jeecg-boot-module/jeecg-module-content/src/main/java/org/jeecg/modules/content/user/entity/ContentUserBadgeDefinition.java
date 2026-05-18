package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区用户勋章定义实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_badge_definition")
@Schema(description = "内容社区用户勋章定义")
public class ContentUserBadgeDefinition extends JeecgEntity {

    @Schema(description = "勋章编码")
    private String badgeCode;

    @Schema(description = "勋章名称")
    private String badgeName;

    @Schema(description = "勋章类型")
    private String badgeType;

    @Schema(description = "勋章分类")
    private String category;

    @Schema(description = "勋章图标URL")
    private String iconUrl;

    @Schema(description = "展示特效KEY")
    private String effectKey;

    @Schema(description = "获得条件说明")
    private String conditionDescription;

    @Schema(description = "发放规则配置JSON")
    private String ruleConfigJson;

    @Schema(description = "有效期天数")
    private Integer validDays;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否自动发放")
    private Boolean autoGrant;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
