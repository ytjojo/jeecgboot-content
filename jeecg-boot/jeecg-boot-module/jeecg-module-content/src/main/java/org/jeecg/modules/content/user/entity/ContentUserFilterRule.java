package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 内容社区用户屏蔽规则。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_filter_rule")
@Schema(description = "内容社区用户屏蔽规则")
public class ContentUserFilterRule extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "规则类型")
    private String ruleType;

    @Schema(description = "规则原始值")
    private String ruleValue;

    @Schema(description = "规则归一化值")
    private String normalizedValue;

    @Schema(description = "匹配范围")
    private String matchScope;

    @Schema(description = "过期时间")
    private Date expiresAt;

    @Schema(description = "规则状态")
    private String status;
}
